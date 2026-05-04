package com.joel.java_scheduler.api.dto;

import com.joel.java_scheduler.core.entity.TemplateOptionDefinition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateJobDefinitionRequest (
    @Valid
    @NotNull(message = "metadata is required")
    Metadata metadata,

    @NotBlank(message = "template is required")
    String template,

    List<TemplateOptionDefinition> options,

    String cronExpression,

    @NotNull(message = "maxRetries is required")
    @Min(value = 0, message = "maxRetries must be greater than or equal to 0")
    Integer maxRetries,

    Boolean enabled
) {
    public record Metadata(
        @NotBlank(message = "metadata.name is required")
        String name,

        @NotBlank(message = "metadata.author is required")
        String author,

        String description
    ) {}
}
