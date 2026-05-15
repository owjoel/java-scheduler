package com.joel.omnicron.worker.controller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.joel.omnicron.core.entity.Job;
import com.joel.omnicron.core.entity.JobCompletionStrategy;
import com.joel.omnicron.core.entity.JobFanOutSpec;
import com.joel.omnicron.core.entity.JobStatus;
import com.joel.omnicron.core.entity.Task;
import com.joel.omnicron.core.entity.TaskStatus;
import com.joel.omnicron.worker.config.WorkerIdentity;
import com.joel.omnicron.worker.util.OperationResult;
import com.joel.omnicron.worker.util.TemplateRenderer;
import com.joel.omnicron.worker.util.YamlMapper;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobSpec;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;

@Component
public class JobReconciler {
    private static final Logger log = LoggerFactory.getLogger(JobReconciler.class);

    private final JobExecutionStore store;

    private final BatchV1Api batchV1Api;
    private final KubernetesJobInformer informer;

    private final WorkerIdentity identity;

    @Value("${worker.kubernetes.namespace:default}")
    private String namespace;

    @Value("${worker.kubernetes.ttlSecondsAfterFinished:3600}")
    private Integer ttlSecondsAfterFinished;

    public JobReconciler(
            JobExecutionStore store,
            KubernetesJobInformer informer,
            BatchV1Api batchV1Api,
            WorkerIdentity identity) {
        this.store = store;
        this.informer = informer;
        this.batchV1Api = batchV1Api;
        this.identity = identity;
    }

    public boolean reconcile(Long jobId) {
        Job job = store.findJob(jobId);
        if (job == null) {
            return true;
        }

        if (!job.isLockedBy(identity.getId()) || !job.hasActiveLock()) {
            return true;
        }

        if (job.isTerminal()) {
            return true;
        }

        JobStatus status = job.getState().getStatus();
        if (status == JobStatus.QUEUED) {
            expandTasks(job);
            return false;
        }

        if (status == JobStatus.RUNNING) {
            return syncRunningJob(job);
        }

        if (status == JobStatus.COMPLETING || status == JobStatus.FAILING) {
            return cleanupJob(job);
        }

        return false;
    }

    private void expandTasks(Job job) {
        List<Task> tasks = createTasks(job);
        log.info("Expanding job {} into {} task(s)", job.getId(), tasks.size());
        store.expandJob(job, tasks);
    }

    private List<Task> createTasks(Job job) {
        List<Task> tasks = new ArrayList<>();
        JobFanOutSpec fanOutSpec = job.getFanOutSpec();

        if (fanOutSpec == null) {
            tasks.add(Task.singleQueued(job.getId()));
            return tasks;
        }

        List<String> keys = fanOutSpec.getWithKeys();
        Integer count = fanOutSpec.getWithCount();

        if (keys != null && !keys.isEmpty()) {
            for (int i = 0; i < keys.size(); i++) {
                tasks.add(Task.queuedAttempt(job.getId(), i, keys.get(i), 1));
            }
            return tasks;
        }

        if (count != null && count > 0) {
            for (int i = 0; i < count; i++) {
                tasks.add(Task.queuedAttempt(job.getId(), i, null, 1));
            }
            return tasks;
        }

        tasks.add(Task.singleQueued(job.getId()));
        return tasks;
    }

    private boolean syncRunningJob(Job job) {
        if (!informer.hasSynced()) {
            return false;
        }

        List<Task> tasks = store.findTasks(job);

        Map<Integer, Task> latestTasks = currentTasksByIndex(tasks);
        boolean changed = false;
        for (Task task : latestTasks.values()) {
            if (!task.isTerminal()) {
                changed = changed || syncTask(job, task);
            }
        }

        if (changed) {
            return false;
        }

        Map<Integer, ShardState> states = coalesceShardStates(job, tasks);
        JobCompletionStrategy strategy = completionStrategy(job);

        switch (strategy) {
            case ANY_SUCCESSFUL:
                return syncAnySuccessfulJob(job, states);
            default:
                return syncAllSuccessfulJob(job, states);
        }
    }

    private JobCompletionStrategy completionStrategy(Job job) {
        if (job.getFanOutSpec() == null || job.getFanOutSpec().getCompletionStrategy() == null) {
            return JobCompletionStrategy.ALL_SUCCESSFUL;
        }

        return job.getFanOutSpec().getCompletionStrategy();
    }

    private Map<Integer, ShardState> coalesceShardStates(Job job, List<Task> tasks) {
        int maxAttempts = job.getMaxRetries() + 1;

        Map<Integer, List<Task>> tasksByIndex = tasks.stream()
                .collect(Collectors.groupingBy(Task::getTaskIndex));

        Map<Integer, ShardState> result = new HashMap<>();

        for (Map.Entry<Integer, List<Task>> entry : tasksByIndex.entrySet()) {
            List<Task> shardTasks = entry.getValue();

            boolean completed = shardTasks.stream()
                    .anyMatch(task -> task.getState().getStatus() == TaskStatus.COMPLETED);

            if (completed) {
                result.put(entry.getKey(), ShardState.COMPLETED);
                continue;
            }

            Task latestAttempt = shardTasks.stream()
                    .max(Comparator.comparing(Task::getAttempt))
                    .orElseThrow();

            boolean retryLimitReached = latestAttempt.getState().getStatus() == TaskStatus.FAILED
                    && latestAttempt.getAttempt() >= maxAttempts;

            if (retryLimitReached) {
                result.put(entry.getKey(), ShardState.RETRY_LIMIT_REACHED);
            } else {
                result.put(entry.getKey(), ShardState.ACTIVE);
            }
        }
        return result;
    }

    private boolean syncTask(Job job, Task task) {
        V1Job k8sJob = informer.getJob(task.getKubernetesJobName());
        if (k8sJob == null) {
            Map<String, Object> context = buildTaskContext(task, job.getOptionValues());
            log.info("Rendering manifest for job {}, task {}, index {}, attempt {}",
                    job.getId(), task.getId(), task.getTaskIndex(), task.getAttempt());
            String manifest = TemplateRenderer.render(job.getTemplate(), context);
            task.setManifest(manifest);

            log.info("Creating Kubernetes Job {} for job {}, task {}",
                    task.getKubernetesJobName(), job.getId(), task.getId());
            OperationResult createJob = createKubernetesJob(task);
            switch (createJob) {
                case PERMANENT_ERROR:
                    log.warn("Permanent error creating Kubernetes Job {} for job {}, task {}",
                            task.getKubernetesJobName(), job.getId(), task.getId());
                    store.failTaskAndJob(task, job, "Invalid rendered Kubernetes manifest");
                    return true;
                case RETRYABLE_ERROR:
                    log.warn("Retryable error creating Kubernetes Job {} for job {}, task {}",
                            task.getKubernetesJobName(), job.getId(), task.getId());
                    return false;
                default:
            }

            log.info("Kubernetes Job {} created for job {}, task {}",
                    task.getKubernetesJobName(), job.getId(), task.getId());

            task.updateState(TaskStatus.ADMITTED, null);
            store.saveTask(task);
            return true;
        }

        if (isKubernetesJobComplete(k8sJob)) {
            log.info("Task {} for job {} completed", task.getId(), job.getId());
            task.updateState(TaskStatus.COMPLETED, null);
            store.saveTask(task);
            return true;
        }

        if (isKubernetesJobFailed(k8sJob)) {
            log.info("Task {} for job {} failed, reconciling next attempt", task.getId(), job.getId());
            store.failTaskAndCreateNextAttempt(task, job);
            return true;
        }

        if (task.getState().getStatus() != TaskStatus.RUNNING) {
            task.updateState(TaskStatus.RUNNING, null);
            store.saveTask(task);
            return true;
        }

        return false;
    }

    private OperationResult createKubernetesJob(Task task) {
        V1PodTemplateSpec podTemplate;
        try {
            podTemplate = YamlMapper.
                readValue(task.getManifest(), V1PodTemplateSpec.class);
        } catch (IOException e) {
            return OperationResult.PERMANENT_ERROR;
        }

        if (podTemplate.getSpec() == null) {
            return OperationResult.PERMANENT_ERROR;
        }

        Map<String, String> labels = Map.of(
                "app.kubernetes.io/managed-by", "omnicron",
                "app.kubernetes.io/part-of", "omnicron",
                "omnicron/task-id", task.getId().toString(),
                "omnicron/job-id", task.getJobId().toString(),
                "omnicron/task-index", task.getTaskIndex().toString(),
                "omnicron/attempt", task.getAttempt().toString());
    

        Map<String, String> annotations = new HashMap<>();
        if (task.getTaskKey() != null) {
            annotations.put("omnicron/task-key", task.getTaskKey());
        }

        podTemplate.getSpec().restartPolicy("Never");
        podTemplate.metadata(new V1ObjectMeta().labels(labels).annotations(annotations));

        V1ObjectMeta metadata = new V1ObjectMeta()
                .name(task.getKubernetesJobName())
                .namespace(namespace)
                .labels(labels)
                .annotations(annotations);

        V1JobSpec jobSpec = new V1JobSpec()
                .backoffLimit(0)
                .ttlSecondsAfterFinished(ttlSecondsAfterFinished)
                .completions(1)
                .parallelism(1)
                .template(podTemplate);

        V1Job k8sJob = new V1Job()
                .apiVersion("batch/v1")
                .kind("Job")
                .metadata(metadata)
                .spec(jobSpec);

        try {
            batchV1Api
                .createNamespacedJob(namespace, k8sJob)
                .execute();
            return OperationResult.SUCCESS;
        } catch (ApiException e) {
            if (e.getCode() == HttpURLConnection.HTTP_CONFLICT) {
                return OperationResult.SUCCESS;
            }
            return OperationResult.RETRYABLE_ERROR;
        } catch (Exception e) {
            return OperationResult.RETRYABLE_ERROR;
        }
    }

    private boolean deleteKubernetesJob(Task task) {
        try {
            batchV1Api
                    .deleteNamespacedJob(task.getKubernetesJobName(), namespace)
                    .propagationPolicy("Background")
                    .execute();
            return true;
        } catch (ApiException e) {
            if (e.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return true;
            }
            return false;
        }
    }

    private Map<String, Object> buildTaskContext(Task task, Map<String, Object> optionValues) {
        Map<String, Object> context = new HashMap<>(optionValues);
        Map<String, Object> taskContext = new HashMap<>();
        taskContext.put("index", task.getTaskIndex());
        taskContext.put("attempt", task.getAttempt());

        if (task.getTaskKey() != null) {
            taskContext.put("key", task.getTaskKey());
        }

        context.put("task", taskContext);
        return context;
    }

    private boolean syncAllSuccessfulJob(Job job, Map<Integer, ShardState> shardStates) {
        boolean allCompleted = shardStates.values().stream()
                .allMatch(state -> state == ShardState.COMPLETED);

        if (allCompleted) {
            log.info("Job {} completed successfully, entering cleanup", job.getId());
            job.updateState(JobStatus.COMPLETING, null, null, Instant.now());
            store.saveJob(job);
            return false;
        }

        boolean anyFailed = shardStates.values().stream()
                .anyMatch(state -> state == ShardState.RETRY_LIMIT_REACHED);

        if (anyFailed) {
            log.info("Job {} failed because one or more tasks reached retry limit, entering cleanup", job.getId());
            job.updateState(JobStatus.FAILING, "One or more tasks failed", null, Instant.now());
            store.saveJob(job);
            return false;
        }

        return false;
    }

    private boolean syncAnySuccessfulJob(Job job, Map<Integer, ShardState> shardStates) {
        boolean anyCompleted = shardStates.values().stream()
                .anyMatch(state -> state == ShardState.COMPLETED);

        if (anyCompleted) {
            log.info("Job {} completed because at least one task succeeded, entering cleanup", job.getId());
            job.updateState(JobStatus.COMPLETING, null, null, Instant.now());
            store.saveJob(job);
            return false;
        }

        boolean allFailed = shardStates.values().stream()
                .allMatch(state -> state == ShardState.RETRY_LIMIT_REACHED);

        if (allFailed) {
            log.info("Job {} failed because all tasks reached retry limit, entering cleanup", job.getId());
            job.updateState(JobStatus.FAILING, "All tasks failed", null, Instant.now());
            store.saveJob(job);
            return false;
        }

        return false;
    }

    private boolean cleanupJob(Job job) {
        List<Task> tasks = store.findTasks(job);
        boolean done = true;
        for (Task task : tasks) {
            if (task.isTerminal()) {
                continue;
            }

            boolean deleted = deleteKubernetesJob(task);
            if (deleted) {
                log.info("Cleaned up Kubernetes Job {} for job {}, task {}",
                        task.getKubernetesJobName(), job.getId(), task.getId());
                task.updateState(TaskStatus.CANCELLED, "Parent job initiated clean up.");
                store.saveTask(task);
            } else {
                done = false;
            }
        }

        if (!done) {
            return false;
        }

        switch (job.getState().getStatus()) {
            case JobStatus.COMPLETING -> {
                log.info("Job {} is terminal: COMPLETED", job.getId());
                job.updateState(JobStatus.COMPLETED, null, null, Instant.now());
            }
            case JobStatus.FAILING -> {
                log.info("Job {} is terminal: FAILED", job.getId());
                job.updateState(JobStatus.FAILED, null, null, Instant.now());
            }
            default -> {
                return false;
            }
        }
        store.saveJob(job);
        return true;
    }

    private Map<Integer, Task> currentTasksByIndex(List<Task> tasks) {
        return tasks.stream()
                .collect(Collectors.toMap(
                        Task::getTaskIndex,
                        task -> task,
                        (left, right) -> left.getAttempt() >= right.getAttempt() ? left : right));
    }

    private boolean isKubernetesJobComplete(V1Job job) {
        if (job.getStatus() == null || job.getStatus().getConditions() == null) {
            return false;
        }

        return job.getStatus().getConditions().stream()
                .anyMatch(condition -> condition.getType().equals("Complete")
                        && condition.getStatus().equals("True"));
    }

    private boolean isKubernetesJobFailed(V1Job job) {
        if (job.getStatus() == null || job.getStatus().getConditions() == null) {
            return false;
        }

        return job.getStatus().getConditions().stream()
                .anyMatch(condition -> condition.getType().equals("Failed")
                        && condition.getStatus().equals("True"));
    }
}

enum ShardState {
    COMPLETED,
    RETRY_LIMIT_REACHED,
    ACTIVE
}
