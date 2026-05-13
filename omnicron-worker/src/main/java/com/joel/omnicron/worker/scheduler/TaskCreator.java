package com.joel.omnicron.worker.scheduler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.joel.omnicron.core.entity.Job;
import com.joel.omnicron.core.entity.JobFanOutSpec;
import com.joel.omnicron.core.entity.JobStatus;
import com.joel.omnicron.core.entity.Task;
import com.joel.omnicron.core.entity.TaskState;
import com.joel.omnicron.core.entity.TaskStatus;
import com.joel.omnicron.core.repository.JobRepository;
import com.joel.omnicron.core.repository.TaskRepository;


@Component
public class TaskCreator {
    public final TaskRepository taskRepository;
    public final JobRepository jobRepository;

    @Value("${worker.task-creator.batch-size:10}")
    private int batchSize;

    public TaskCreator(TaskRepository taskRepository, JobRepository jobRepository) {
        this.taskRepository = taskRepository;
        this.jobRepository = jobRepository;
    }

    @Scheduled(fixedDelayString = "${worker.task-creator.interval:1000}")
    @Transactional
    public void createTasks() {
        List<Job> jobs = jobRepository.claimQueuedJobs(batchSize);

        for (Job job : jobs) {
            List<Task> taskList = new ArrayList<>();
            JobFanOutSpec fanOutSpec = job.getFanOutSpec();
            if (fanOutSpec == null) {
                taskList.add(Task.singleQueued(
                    job.getId(),
                    buildKubernetesJobName(job, 0, 1)));
            } else {
                taskList.addAll(createFanOutTasks(fanOutSpec, job));
            }

            taskRepository.saveAll(taskList);
            job.updateState(JobStatus.RUNNING, null, Instant.now(), null);
        }
    }

    private List<Task> createFanOutTasks(JobFanOutSpec fanOutSpec, Job job) {
        List<Task> tasks = new ArrayList<>();
        List<String> keys = fanOutSpec.getWithKeys();
        Integer count = fanOutSpec.getWithCount();
        if (keys != null && !keys.isEmpty()) {
            for (int i = 0; i < keys.size(); i++) {
                Task task = new Task(
                    job.getId(),
                    i,
                    keys.get(i),
                    1,
                    buildKubernetesJobName(job, i, 1),
                    new TaskState(TaskStatus.QUEUED, null, null, null));
                tasks.add(task);
            }
        } else if (count != null && count > 0) {
            for (int i = 0; i < count; i++) {
                Task task = new Task(
                    job.getId(),
                    i,
                    null,
                    1,
                    buildKubernetesJobName(job, i, 1),
                    TaskState.queued());
                tasks.add(task);
            }
        // If no fan-out object, default to single Task
        } else {
            tasks.add(Task.singleQueued(
                job.getId(),
                buildKubernetesJobName(job, 0, 1)));
        }
        return tasks;
    }

    private String buildKubernetesJobName(Job job, int taskIndex, int attempt) {
        return "omnicron-job-" + job.getId()
                + "-task-" + taskIndex
                + "-attempt-" + attempt;
    }
}
