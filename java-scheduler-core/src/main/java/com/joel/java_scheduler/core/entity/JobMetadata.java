package com.joel.java_scheduler.core.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class JobMetadata {
    private String name;
    private String operator;

    protected JobMetadata() {
    }

    public JobMetadata(String name, String operator) {
        this.name = name;
        this.operator = operator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
