package com.joel.omnicron.api.dto;

import com.joel.omnicron.core.entity.JobFanOutSpec;
import com.joel.omnicron.core.entity.JobStatus;
import com.joel.omnicron.core.entity.JobType;
import java.time.Instant;
import java.util.Map;

public record GetJobResponse(
        Long id,
        Long jobDefinitionId,
        Metadata metadata,
        JobType jobType,
        Map<String, Object> optionValues,
        Integer maxRetries,
        JobFanOutSpec fanOutSpec,
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
