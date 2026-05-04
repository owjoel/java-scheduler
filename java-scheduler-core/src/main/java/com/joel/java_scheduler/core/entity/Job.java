package com.joel.java_scheduler.core.entity;

import java.time.Instant;
import java.util.Map;

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
    private Map<String, String> optionValues;

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
            Map<String, String> optionValues,
            JobState state) {
        this.jobDefinitionId = jobDefinitionId;
        this.metadata = metadata;
        this.jobType = jobType;
        this.optionValues = optionValues;
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

    public Map<String, String> getOptionValues() {
        return optionValues;
    }

    public void setOptionValues(Map<String, String> optionValues) {
        this.optionValues = optionValues;
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
}
