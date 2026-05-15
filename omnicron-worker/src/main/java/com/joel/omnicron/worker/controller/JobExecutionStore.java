package com.joel.omnicron.worker.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.joel.omnicron.core.entity.Job;
import com.joel.omnicron.core.entity.JobStatus;
import com.joel.omnicron.core.entity.Task;
import com.joel.omnicron.core.entity.TaskStatus;
import com.joel.omnicron.core.repository.JobRepository;
import com.joel.omnicron.core.repository.TaskRepository;

@Component
public class JobExecutionStore {
    private final JobRepository jobRepository;
    private final TaskRepository taskRepository;

    public JobExecutionStore(JobRepository jobRepository, TaskRepository taskRepository) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
    }

    public Job findJob(Long jobId) {
        return jobRepository.findById(jobId).orElse(null);
    }

    public List<Task> findTasks(Job job) {
        return taskRepository.findByJobIdOrderByTaskIndexAscAttemptAsc(job.getId());
    }

    public Job saveJob(Job job) {
        return jobRepository.save(job);
    }

    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    public void expandJob(Job job, List<Task> tasks) {
        taskRepository.saveAll(tasks);
        job.updateState(JobStatus.RUNNING, null, Instant.now(), null);
        jobRepository.save(job);
    }

    @Transactional
    public void failTaskAndJob(Task task, Job job, String reason) {
        task.updateState(TaskStatus.FAILED, reason);
        taskRepository.save(task);

        job.updateState(JobStatus.FAILING, reason, null, null);
        jobRepository.save(job);
    }

    @Transactional
    public void failTaskAndCreateNextAttempt(Task task, Job job) {
        task.updateState(TaskStatus.FAILED, null);
        taskRepository.save(task);

        Integer nextAttemptIndex = task.getAttempt() + 1;
        if (nextAttemptIndex > job.getMaxRetries() + 1) {
            return;
        }
        if (taskRepository.existsByJobIdAndTaskIndexAndAttempt(
                job.getId(),
                task.getTaskIndex(),
                nextAttemptIndex)) {
            return;
        }

        Task nextAttempt = Task.queuedAttempt(
                job.getId(),
                task.getTaskIndex(),
                task.getTaskKey(),
                nextAttemptIndex);

        try {
            taskRepository.saveAndFlush(nextAttempt);
        } catch (DataIntegrityViolationException e) {
        }
    }
}
