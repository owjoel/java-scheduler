package com.joel.omnicron.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;

@Embeddable
public class JobState {
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(name = "failure_message")
    private String failureMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    protected JobState() {
    }

    public JobState(JobStatus status, String failureMessage, Instant startedAt, Instant endedAt) {
        this.status = status;
        this.failureMessage = failureMessage;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
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

    public void setEnd(Instant endedAt) {
        this.endedAt = endedAt;
    }
}
