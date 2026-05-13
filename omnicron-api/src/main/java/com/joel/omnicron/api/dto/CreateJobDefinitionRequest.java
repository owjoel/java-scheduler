package com.joel.omnicron.api.dto;

import com.joel.omnicron.core.entity.JobFanOutSpec;
import com.joel.omnicron.core.entity.TemplateOptionType;
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

    @Valid
    List<TemplateOption> options,

    Schedule schedule,

    @NotNull(message = "maxRetries is required")
    @Min(value = 0, message = "maxRetries must be greater than or equal to 0")
    Integer maxRetries,

    JobFanOutSpec fanOutSpec
) {
    public record Metadata(
        @NotBlank(message = "metadata.name is required")
        String name,

        @NotBlank(message = "metadata.author is required")
        String author,

        String description
    ) {}

    public record Schedule(
        String cronExpression,
        Boolean scheduleEnabled
    ) {}

    public record TemplateOption(
        @NotBlank(message = "option.name is required")
        String name,

        @NotNull(message = "option.type is required")
        TemplateOptionType type,

        Boolean required,

        String defaultValue
    ) {}
}
