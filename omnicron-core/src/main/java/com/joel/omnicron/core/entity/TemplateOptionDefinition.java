package com.joel.omnicron.core.entity;

public record TemplateOptionDefinition(
        String name,
        TemplateOptionType type,
        Boolean required,
        String defaultValue) {
}
