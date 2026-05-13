package com.joel.omnicron.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "job_definitions")
public class JobDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_definitions_seq")
    @SequenceGenerator(
        name = "job_definitions_seq",
        sequenceName = "job_definitions_seq",
        allocationSize = 50
    )
    private Long id;

    @Embedded
    private JobDefinitionMetadata metadata;

    @Column(columnDefinition = "text")
    private String template;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<TemplateOptionDefinition> options;

    @Column(name = "max_retries")
    private Integer maxRetries;

    @Embedded
    private JobFanOutSpec fanOutSpec;

    @Embedded
    private JobDefinitionSchedule schedule;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected JobDefinition() {
    }

    public JobDefinition(
        JobDefinitionMetadata metadata,
        String template,
        List<TemplateOptionDefinition> options,
        Integer maxRetries,
        JobFanOutSpec fanOutSpec,
        JobDefinitionSchedule schedule) {
        this.metadata = metadata;
        this.template = template;
        this.options = options;
        this.maxRetries = maxRetries;
        this.fanOutSpec = fanOutSpec;
        this.schedule = schedule;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public JobDefinitionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(JobDefinitionMetadata metadata) {
        this.metadata = metadata;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<TemplateOptionDefinition> getOptions() {
        return options;
    }

    public void setOptions(List<TemplateOptionDefinition> options) {
        this.options = options;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public JobFanOutSpec getFanOutSpec() {
        return fanOutSpec;
    }

    public void setFanOutSpec(JobFanOutSpec fanOutSpec) {
        this.fanOutSpec = fanOutSpec;
    }

    public JobDefinitionSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(JobDefinitionSchedule schedule) {
        this.schedule = schedule;
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
