package com.joel.java_scheduler.core.entity;

public record TemplateOptionDefinition(
        String name,
        TemplateOptionType type,
        Boolean required,
        String defaultValue) {
}
