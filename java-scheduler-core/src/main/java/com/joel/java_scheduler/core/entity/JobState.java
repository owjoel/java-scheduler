package com.joel.java_scheduler.core.entity;

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

    private Instant start;

    @Column(name = "ended_at")
    private Instant end;

    protected JobState() {
    }

    public JobState(JobStatus status, String failureMessage, Instant start, Instant end) {
        this.status = status;
        this.failureMessage = failureMessage;
        this.start = start;
        this.end = end;
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

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }
}
