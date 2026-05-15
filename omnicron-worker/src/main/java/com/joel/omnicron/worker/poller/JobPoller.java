package com.joel.omnicron.worker.poller;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.joel.omnicron.core.repository.JobRepository;
import com.joel.omnicron.worker.config.WorkerIdentity;

import io.kubernetes.client.extended.workqueue.RateLimitingQueue;

@Component
public class JobPoller {
    private final JobRepository jobRepository;
    private final RateLimitingQueue<Long> jobQueue;
    private final WorkerIdentity workerIdentity;

    @Value("${worker.poller.batch-size:10}")
    private int batchSize;

    @Value("${worker.poller.lock-duration:30s}")
    private Duration lockDuration;

    public JobPoller(
            JobRepository jobRepository,
            RateLimitingQueue<Long> jobQueue,
            WorkerIdentity workerIdentity) {
        this.jobRepository = jobRepository;
        this.jobQueue = jobQueue;
        this.workerIdentity = workerIdentity;
    }

    @Scheduled(fixedDelayString = "${worker.poller.interval:1000}")
    public void claimJobs() {
        Instant lockedUntil = Instant.now().plus(lockDuration);

        List<Long> jobIds = jobRepository.claimJobs(
                workerIdentity.getId(),
                lockedUntil,
                batchSize);

        for (Long jobId : jobIds) {
            jobQueue.add(jobId);
        }
    }
}
