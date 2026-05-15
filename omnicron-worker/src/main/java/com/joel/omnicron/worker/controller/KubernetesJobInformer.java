package com.joel.omnicron.worker.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.kubernetes.client.extended.workqueue.RateLimitingQueue;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobList;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class KubernetesJobInformer {
    private final SharedInformerFactory informerFactory;
    private final BatchV1Api batchV1Api;
    private final RateLimitingQueue<Long> queue;

    private SharedIndexInformer<V1Job> informer;
    private Lister<V1Job> jobLister;

    @Value("${worker.kubernetes.namespace:default}")
    private String namespace;

    public KubernetesJobInformer(
            ApiClient apiClient,
            BatchV1Api batchV1Api,
            RateLimitingQueue<Long> queue) {
        apiClient.setReadTimeout(0);
        this.informerFactory = new SharedInformerFactory(apiClient, true);
        this.batchV1Api = batchV1Api;
        this.queue = queue;
    }

    @PostConstruct
    public void start() {
        informer =
            informerFactory.sharedIndexInformerFor(
                params -> batchV1Api
                    .listNamespacedJob(namespace)
                    .labelSelector("app.kubernetes.io/managed-by=omnicron")
                    .resourceVersion(params.resourceVersion)
                    .timeoutSeconds(params.timeoutSeconds)
                    .watch(params.watch)
                    .buildCall(null),
                V1Job.class,
                V1JobList.class,
                0L);
        
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override
            public void onAdd(V1Job job) {
                enqueue(job);
            }

            @Override
            public void onUpdate(V1Job oldJob, V1Job newJob) {
                enqueue(newJob);
            }

            @Override
            public void onDelete(V1Job job, boolean deletedFinalStateUnknown) {
                enqueue(job);
            }
        });

        jobLister = new Lister<>(informer.getIndexer()).namespace(namespace);

        informerFactory.startAllRegisteredInformers();
    }

    public boolean hasSynced() {
        return informer != null && informer.hasSynced();
    }

    private void enqueue(V1Job job) {
        if (job.getMetadata() == null) {
            return;
        }

        Map<String, String> labels = job.getMetadata().getLabels();
        if (labels == null) {
            return;
        }

        String jobId = labels.get("omnicron/job-id");
        if (jobId == null) {
            return;
        }

        try {
            queue.add(Long.valueOf(jobId));
        } catch (NumberFormatException e) {

        }
    }

    public V1Job getJob(String name) {
        if (jobLister == null || name == null) {
            return null;
        }

        return jobLister.get(name);
    }

    @PreDestroy
    public void stop() {
        informerFactory.stopAllRegisteredInformers();
    }
}
