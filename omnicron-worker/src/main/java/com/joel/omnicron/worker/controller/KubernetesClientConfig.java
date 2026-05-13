package com.joel.omnicron.worker.controller;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.util.Config;

@Configuration
public class KubernetesClientConfig {
    @Bean
    public ApiClient client() throws IOException {
        ApiClient client = Config.defaultClient();
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        return client;
    }

    @Bean
    public BatchV1Api batchV1Api(ApiClient apiClient) {
        return new BatchV1Api(apiClient);
    }
}
