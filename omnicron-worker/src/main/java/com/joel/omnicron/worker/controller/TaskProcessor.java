package com.joel.omnicron.worker.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import io.kubernetes.client.extended.workqueue.RateLimitingQueue;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class TaskProcessor {
    private final RateLimitingQueue<Long> taskQueue;
    private final TaskReconciler reconciler;

    private ExecutorService executorService;

    @Value("${worker.processor.thread-count:3}")
    private int threadCount;

    public TaskProcessor(
            RateLimitingQueue<Long> taskQueue,
            TaskReconciler reconciler) {
        this.taskQueue = taskQueue;
        this.reconciler = reconciler;
    }

    @PostConstruct
    public void start() {
        executorService = Executors.newFixedThreadPool(
                threadCount,
                new CustomizableThreadFactory("task-worker-"));
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(this::workerLoop);
        }
    }

    private void workerLoop() {
        while (!Thread.currentThread().isInterrupted() && !taskQueue.isShuttingDown()) {
            Long taskId;
            try {
                taskId = taskQueue.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            try {
                boolean finished = reconciler.reconcile(taskId);
                if (finished) {
                    taskQueue.forget(taskId);
                } else {
                    taskQueue.addRateLimited(taskId);
                }
            } catch (Exception e) {
                taskQueue.addRateLimited(taskId);
            } finally {
                taskQueue.done(taskId);
            }
        }
    }

    @PreDestroy
    public void stop() {
        taskQueue.shutDown();
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
