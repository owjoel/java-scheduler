package com.joel.omnicron.api.dto;

import com.joel.omnicron.core.entity.JobFanOutSpec;
import com.joel.omnicron.core.entity.TemplateOptionDefinition;
import java.time.Instant;
import java.util.List;

public record GetJobDefinitionResponse(
    Long id,
    Metadata metadata,
    String template,
    List<TemplateOptionDefinition> options,
    Integer maxRetries,
    JobFanOutSpec fanOutSpec,
    Schedule schedule,
    Instant createdAt,
    Instant updatedAt
) {
    public record Metadata(
        String name,
        String author,
        String description
    ) {
    }

    public record Schedule(
        String cronExpression,
        Boolean scheduleEnabled,
        Instant lastScheduledAt,
        Instant nextScheduledAt
    ) {
    }
}
