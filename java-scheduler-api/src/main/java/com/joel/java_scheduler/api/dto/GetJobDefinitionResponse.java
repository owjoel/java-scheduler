package com.joel.java_scheduler.api.dto;

import com.joel.java_scheduler.core.entity.TemplateOptionDefinition;
import java.time.Instant;
import java.util.List;

public record GetJobDefinitionResponse(
    Long id,
    Metadata metadata,
    String template,
    List<TemplateOptionDefinition> options,
    String cronExpression,
    Integer maxRetries,
    Boolean enabled,
    Instant createdAt,
    Instant updatedAt
) {
    public record Metadata(
        String name,
        String author,
        String description
    ) {
    }
}
