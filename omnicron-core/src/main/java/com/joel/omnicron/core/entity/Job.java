package com.joel.omnicron.core.entity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jobs_seq")
    @SequenceGenerator(name = "jobs_seq", sequenceName = "jobs_seq", allocationSize = 50)
    private Long id;

    @Column(name = "job_definition_id", nullable = false)
    private Long jobDefinitionId;

    @Embedded
    private JobMetadata metadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "option_values")
    private Map<String, Object> optionValues;

    @Column(name = "template", nullable = false)
    private String template;

    @Embedded
    private JobFanOutSpec fanOutSpec;
    


    @Embedded
    private JobState state;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Job() {
    }

    public Job(
            Long jobDefinitionId,
            JobMetadata metadata,
            JobType jobType,
            Map<String, Object> optionValues,
            String template,
            JobFanOutSpec fanOutSpec,
            JobState state) {
        this.jobDefinitionId = jobDefinitionId;
        this.metadata = metadata;
        this.jobType = jobType;
        this.optionValues = optionValues != null ? new HashMap<>(optionValues) : new HashMap<>();
        this.template = template;
        this.fanOutSpec = fanOutSpec;
        this.state = state;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getJobDefinitionId() {
        return jobDefinitionId;
    }

    public void setJobDefinitionId(Long jobDefinitionId) {
        this.jobDefinitionId = jobDefinitionId;
    }

    public JobMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(JobMetadata metadata) {
        this.metadata = metadata;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public Map<String, Object> getOptionValues() {
        return optionValues;
    }

    public void setOptionValues(Map<String, Object> optionValues) {
        this.optionValues = Objects.requireNonNull(optionValues);
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public JobFanOutSpec getFanOutSpec() {
        return fanOutSpec;
    }

    public void setFanOutSpec(JobFanOutSpec fanOutSpec) {
        this.fanOutSpec = fanOutSpec;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
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

    public void putOptionValue(String name, Object value) {
        if (this.optionValues == null) {
            this.optionValues = new HashMap<>();
        }
        this.optionValues.put(name, value);
    }

    public Object getOptionValue(String name) {
        return optionValues.get(name);
    }

    public void updateState(
            JobStatus status,
            String failureMessage,
            Instant startedAt,
            Instant endedAt) {
        if (state == null) {
            state = new JobState(null, null, null, null);
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
            state.setEnd(endedAt);
        }
    }

    public boolean isTerminal() {
        return state.getStatus().equals(JobStatus.COMPLETED) || state.getStatus().equals(JobStatus.FAILED);
    }
}
