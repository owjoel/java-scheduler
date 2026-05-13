package com.joel.omnicron.core.entity;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class JobFanOutSpec {

    @Column(name = "with_count")
    private Integer withCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "with_keys")
    private List<String> withKeys;

    public Integer getWithCount() {
        return withCount;
    }

    public void setWithCount(Integer withCount) {
        this.withCount = withCount;
    }

    public List<String> getWithKeys() {
        return withKeys;
    }

    public void setWithKeys(List<String> withKeys) {
        this.withKeys = withKeys;
    }
}
