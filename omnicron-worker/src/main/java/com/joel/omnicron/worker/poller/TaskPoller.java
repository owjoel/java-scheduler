package com.joel.omnicron.worker.poller;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.joel.omnicron.core.repository.TaskRepository;
import com.joel.omnicron.worker.config.WorkerIdentity;

import io.kubernetes.client.extended.workqueue.RateLimitingQueue;

@Component
public class TaskPoller {
    private final TaskRepository taskRepository;
    private final RateLimitingQueue<Long> taskQueue;
    private final WorkerIdentity workerIdentity;

    @Value("${worker.poller.batch-size:20}")
    private int batchSize;

    @Value("${worker.poller.lock-duration:30s}")
    private Duration lockDuration;

    public TaskPoller(TaskRepository taskRepository, RateLimitingQueue<Long> taskQueue, WorkerIdentity workerIdentity) {
        this.taskRepository = taskRepository;
        this.taskQueue = taskQueue;
        this.workerIdentity = workerIdentity;
    }

    @Scheduled(fixedDelayString = "${worker.poller.interval:1000}")
    public void claimQueuedTasks() {
        Instant lockedUntil = Instant.now().plus(lockDuration);
        List<Long> taskIds = taskRepository.claimQueuedTasks(workerIdentity.getId(), lockedUntil, batchSize);

        for (Long taskId : taskIds) {
            taskQueue.add(taskId);
        }
    }
}
