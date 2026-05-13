package com.joel.omnicron.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;

@Embeddable
public class JobDefinitionSchedule {
    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "schedule_enabled")
    private Boolean scheduleEnabled;

    @Column(name = "last_scheduled_at")
    private Instant lastScheduledAt;

    @Column(name = "next_scheduled_at")
    private Instant nextScheduledAt;

    protected JobDefinitionSchedule() {
    }

    public JobDefinitionSchedule(
            String cronExpression,
            Boolean scheduleEnabled,
            Instant lastScheduledAt,
            Instant nextScheduledAt) {
        this.cronExpression = cronExpression;
        this.scheduleEnabled = scheduleEnabled != null ? scheduleEnabled : true;
        this.lastScheduledAt = lastScheduledAt;
        this.nextScheduledAt = nextScheduledAt;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Boolean getScheduleEnabled() {
        return scheduleEnabled;
    }

    public void setScheduleEnabled(Boolean scheduleEnabled) {
        this.scheduleEnabled = scheduleEnabled;
    }

    public Instant getLastScheduledAt() {
        return lastScheduledAt;
    }

    public void setLastScheduledAt(Instant lastScheduledAt) {
        this.lastScheduledAt = lastScheduledAt;
    }

    public Instant getNextScheduledAt() {
        return nextScheduledAt;
    }

    public void setNextScheduledAt(Instant nextScheduledAt) {
        this.nextScheduledAt = nextScheduledAt;
    }
}
