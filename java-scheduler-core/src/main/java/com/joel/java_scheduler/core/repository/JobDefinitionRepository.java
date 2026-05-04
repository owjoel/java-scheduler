package com.joel.java_scheduler.core.repository;

import com.joel.java_scheduler.core.entity.JobDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobDefinitionRepository extends JpaRepository<JobDefinition, Long> {
    List<JobDefinition> findByEnabled(Boolean enabled);

    Optional<JobDefinition> findByMetadataName(String name);
}
