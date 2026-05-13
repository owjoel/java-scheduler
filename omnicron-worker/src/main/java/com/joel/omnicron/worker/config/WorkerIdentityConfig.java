package com.joel.omnicron.worker.config;

import java.net.InetAddress;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerIdentityConfig {
    @Bean
    public WorkerIdentity workerIdentity() throws Exception {
        String hostname = InetAddress.getLocalHost().getHostName();
        return new WorkerIdentity(hostname + "-" + UUID.randomUUID());
    }
}