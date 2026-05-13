package com.joel.omnicron.core.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class JobDefinitionMetadata {
    private String name;
    private String description;
    private String author;

    protected JobDefinitionMetadata() {
    }

    public JobDefinitionMetadata(String name, String author, String description) {
        this.name = name;
        this.author = author;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
