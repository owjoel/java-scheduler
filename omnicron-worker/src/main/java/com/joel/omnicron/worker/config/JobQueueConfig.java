package com.joel.omnicron.worker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.kubernetes.client.extended.workqueue.DefaultRateLimitingQueue;
import io.kubernetes.client.extended.workqueue.RateLimitingQueue;

@Configuration
public class JobQueueConfig {
    @Bean
    public RateLimitingQueue<Long> jobQueue() {
        return new DefaultRateLimitingQueue<>();
    }
}
