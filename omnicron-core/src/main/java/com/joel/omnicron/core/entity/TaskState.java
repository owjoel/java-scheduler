package com.joel.omnicron.core.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class TaskState {
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "failure_message")
    private String failureMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    protected TaskState() {
    }

    public TaskState(TaskStatus status, String failureMessage, Instant startedAt, Instant endedAt) {
        this.status = status;
        this.failureMessage = failureMessage;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    /**
     * Creates the initial state for a Task that is ready to be claimed by a worker.
     */
    public static TaskState queued() {
        return new TaskState(TaskStatus.QUEUED, null, null, null);
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }
}
