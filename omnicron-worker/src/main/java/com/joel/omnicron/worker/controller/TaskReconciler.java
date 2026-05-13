package com.joel.omnicron.worker.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.joel.omnicron.core.entity.Job;
import com.joel.omnicron.core.entity.Task;
import com.joel.omnicron.core.entity.TaskStatus;
import com.joel.omnicron.core.repository.JobRepository;
import com.joel.omnicron.core.repository.TaskRepository;
import com.joel.omnicron.worker.config.WorkerIdentity;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobSpec;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;

@Component
public class TaskReconciler {
    private final TaskRepository taskRepository;
    private final JobRepository jobRepository;
    private final JobTemplateRenderer renderer;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final WorkerIdentity workerIdentity;
    private final BatchV1Api batchV1Api;

    @Value("${worker.kubernetes.namespace:default}")
    private String namespace;

    public TaskReconciler(
            TaskRepository taskRepository,
            JobRepository jobRepository,
            WorkerIdentity workerIdentity,
            JobTemplateRenderer renderer,
            BatchV1Api batchV1Api) {
        this.taskRepository = taskRepository;
        this.jobRepository = jobRepository;
        this.workerIdentity = workerIdentity;
        this.renderer = renderer;
        this.batchV1Api = batchV1Api;
    }

    public boolean reconcile(Long taskId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return false;
        }

        if (task.isTerminal()) {
            return true;
        }

        if (!task.isLockedBy(workerIdentity.getId()) || !task.hasActiveLock()) {
            return true;
        }

        Optional<Job> jobOptional = validateParentJob(task);
        if (jobOptional.isEmpty()) {
            return true;
        }
        Job job = jobOptional.get();

        // Check K8s job running
        V1Job k8sJob;
        try {
            k8sJob = findKubernetesV1Job(task.getKubernetesJobName());
        } catch (ApiException e) {
            // TODO log
            return false;
        }

        if (k8sJob == null) {
            // build k8s job
            Map<String, Object> context = buildTaskContext(task, job.getOptionValues());

            // Render template
            String manifest = renderer.render(job.getTemplate(), context);
            task.setManifest(manifest);

            try {
                createKubernetesJob(task);
            } catch (Exception e) {
                // TODO log
                return false;
            }
            task.updateState(TaskStatus.RUNNING, null, Instant.now(), null);
            taskRepository.save(task);
            return false;
        }

        return false;
    }

    private Optional<Job> validateParentJob(Task task) {
        Optional<Job> job = jobRepository.findById(task.getJobId());

        if (job.isEmpty()) {
            task.updateState(TaskStatus.FAILED,
                    "Job not found for id: " + task.getJobId(),
                    null,
                    Instant.now());
            taskRepository.save(task);
        }

        return job;
    }

    private V1Job findKubernetesV1Job(String name) throws ApiException {
        try {
            return batchV1Api
                    .readNamespacedJob(name, namespace)
                    .execute();
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    private V1Job createKubernetesJob(Task task) throws Exception {
        V1PodTemplateSpec podTemplate = yamlMapper.readValue(task.getManifest(), V1PodTemplateSpec.class);

        Map<String, String> labels = Map.of(
            "app.kubernetes.io/managed-by", "omnicron",
            "app.kubernetes.io/part-of", "omnicron",
            "omnicron/task-id", task.getId().toString(),
            "omnicron/job-id", task.getJobId().toString(),
            "omnicron/task-index", task.getTaskIndex().toString(),
            "omnicron/attempt", task.getAttempt().toString()
        );

        Map<String, String> annotations = new HashMap<>();
        if (task.getTaskKey() != null) {
            annotations.put("omnicron/task-key", task.getTaskKey());
        }

        V1ObjectMeta metadata = new V1ObjectMeta()
                .name(task.getKubernetesJobName())
                .namespace(namespace)
                .labels(labels)
                .annotations(annotations);
        
        V1JobSpec jobSpec = new V1JobSpec()
                .backoffLimit(0)
                .template(podTemplate);

        V1Job k8sJob = new V1Job()
                .apiVersion("batch/v1")
                .kind("Job")
                .metadata(metadata)
                .spec(jobSpec);

        return batchV1Api
                .createNamespacedJob(namespace, k8sJob)
                .execute();
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
}
