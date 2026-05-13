package com.joel.omnicron.worker.config;

public class WorkerIdentity {
    private final String id;

    public WorkerIdentity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
