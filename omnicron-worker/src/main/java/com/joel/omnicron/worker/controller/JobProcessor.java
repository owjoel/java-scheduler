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
public class JobProcessor {
    private final RateLimitingQueue<Long> jobQueue;
    private final JobReconciler reconciler;

    private ExecutorService executorService;

    @Value("${worker.processor.thread-count:3}")
    private int threadCount;

    public JobProcessor(
            RateLimitingQueue<Long> jobQueue,
            JobReconciler reconciler) {
        this.jobQueue = jobQueue;
        this.reconciler = reconciler;
    }

    @PostConstruct
    public void start() {
        executorService = Executors.newFixedThreadPool(
                threadCount,
                new CustomizableThreadFactory("worker-thread-"));
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(this::workerLoop);
        }
    }

    private void workerLoop() {
        while (!Thread.currentThread().isInterrupted() && !jobQueue.isShuttingDown()) {
            Long jobId;
            try {
                jobId = jobQueue.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            try {
                boolean finished = reconciler.reconcile(jobId);
                if (finished) {
                    jobQueue.forget(jobId);
                } else {
                    jobQueue.addRateLimited(jobId);
                }
            } catch (Exception e) {
                jobQueue.addRateLimited(jobId);
            } finally {
                jobQueue.done(jobId);
            }
        }
    }

    @PreDestroy
    public void stop() {
        jobQueue.shutDown();
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
