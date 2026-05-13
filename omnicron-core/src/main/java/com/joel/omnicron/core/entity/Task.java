package com.joel.omnicron.core.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_seq")
    @SequenceGenerator(name = "tasks_seq", sequenceName = "tasks_seq", allocationSize = 50)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "manifest")
    private String manifest;

    @Column(name = "task_index", nullable = false)
    private Integer taskIndex;

    @Column(name = "task_key")
    private String taskKey;

    @Column(name = "attempt", nullable = false)
    private Integer attempt;


    @Column(name = "kubernetes_job_name")
    private String kubernetesJobName;

    @Embedded
    private TaskState state;

    @Column(name = "locked_by")
    private String lockedBy;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Task() {
    }

    public Task(Long jobId, Integer taskIndex, String taskKey, Integer attempt, String kubernetesJobName, TaskState state) {
        this.jobId = jobId;
        this.taskIndex = taskIndex;
        this.taskKey = taskKey;
        this.attempt = attempt;
        this.state = state;
        this.kubernetesJobName = kubernetesJobName;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Creates the default queued Task for a non-fan-out Job.
     */
    public static Task singleQueued(Long jobId, String kubernetesJobName) {
        return new Task(jobId, 0, null, 1, kubernetesJobName, TaskState.queued());
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getManifest() {
        return manifest;
    }

    public void setManifest(String manifest) {
        this.manifest = manifest;
    } 

    public Integer getTaskIndex() {
        return taskIndex;
    }

    public void setTaskIndex(Integer taskIndex) {
        this.taskIndex = taskIndex;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public Integer getAttempt() {
        return attempt;
    }

    public void setAttempt(Integer attempt) {
        this.attempt = attempt;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public String getKubernetesJobName() {
        return kubernetesJobName;
    }

    public void setKubernetesJobName(String kubernetesJobName) {
        this.kubernetesJobName = kubernetesJobName;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isTerminal() {
        return state != null
                && (state.getStatus() == TaskStatus.COMPLETED || state.getStatus() == TaskStatus.FAILED);
    }

    public boolean isLockedBy(String workerId) {
        return workerId != null && workerId.equals(lockedBy);
    }

    public boolean hasActiveLock() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    public void updateState(
            TaskStatus status,
            String failureMessage,
            Instant startedAt,
            Instant endedAt) {
        if (state == null) {
            state = new TaskState(null, null, null, null);
        }

        if (status != null) {
            state.setStatus(status);
        }

        if (failureMessage != null) {
            state.setFailureMessage(failureMessage);
        }

        if (startedAt != null) {
            state.setStartedAt(startedAt);
        }

        if (endedAt != null) {
            state.setEndedAt(endedAt);
        }
    }
}
