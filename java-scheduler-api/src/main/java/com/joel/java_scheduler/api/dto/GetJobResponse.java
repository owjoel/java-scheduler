package com.joel.java_scheduler.api.dto;

import com.joel.java_scheduler.core.entity.JobStatus;
import com.joel.java_scheduler.core.entity.JobType;
import java.time.Instant;
import java.util.Map;

public record GetJobResponse(
        Long id,
        Long jobDefinitionId,
        Metadata metadata,
        JobType jobType,
        Map<String, String> optionValues,
        State state,
        Instant createdAt,
        Instant updatedAt) {
    public record Metadata(
            String name,
            String operator) {
    }

    public record State(
            JobStatus status,
            String failureMessage,
            Instant start,
            Instant end) {
    }
}
