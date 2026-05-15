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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Task() {
    }

    public Task(Long jobId, Integer taskIndex, String taskKey, Integer attempt, String kubernetesJobName,
            TaskState state) {
        this.jobId = jobId;
        this.taskIndex = taskIndex;
        this.taskKey = taskKey;
        this.attempt = attempt;
        this.state = state;
        this.kubernetesJobName = kubernetesJobName;
    }

    /**
     * Creates the default queued Task for a non-fan-out Job.
     */
    public static Task singleQueued(Long jobId) {
        return queuedAttempt(jobId, 0, null, 1);
    }

    /**
     * Creates a queued Task attempt and assigns its deterministic Kubernetes Job
     * name.
     */
    public static Task queuedAttempt(Long jobId, Integer taskIndex, String taskKey, Integer attempt) {
        return new Task(
                jobId,
                taskIndex,
                taskKey,
                attempt,
                buildKubernetesJobName(jobId, taskIndex, attempt),
                TaskState.queued());
    }

    public static String buildKubernetesJobName(Long jobId, Integer taskIndex, Integer attempt) {
        return "omnicron-job-" + jobId
                + "-task-" + taskIndex
                + "-attempt-" + attempt;
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
        return state != null && isTerminalStatus(state.getStatus());
    }

    public boolean isLockedBy(String workerId) {
        return workerId != null && workerId.equals(lockedBy);
    }

    public boolean hasActiveLock() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    public void updateState(TaskStatus status, String failureMessage) {
        if (state == null) {
            state = new TaskState(null, null, null, null);
        }

        Instant now = Instant.now();

        if (status != null) {
            state.setStatus(status);

            if (isStartedStatus(status) && state.getStartedAt() == null) {
                state.setStartedAt(now);
            }

            if (isTerminalStatus(status) && state.getEndedAt() == null) {
                state.setEndedAt(now);
            }
        }

        if (failureMessage != null) {
            state.setFailureMessage(failureMessage);
        }
    }

    private boolean isStartedStatus(TaskStatus status) {
        return status == TaskStatus.ADMITTED
                || status == TaskStatus.RUNNING
                || isTerminalStatus(status);
    }

    private boolean isTerminalStatus(TaskStatus status) {
        return status == TaskStatus.COMPLETED
                || status == TaskStatus.FAILED
                || status == TaskStatus.CANCELLED;
    }
}
