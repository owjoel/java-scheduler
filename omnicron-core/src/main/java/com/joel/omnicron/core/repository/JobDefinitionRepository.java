package com.joel.omnicron.core.repository;

import com.joel.omnicron.core.entity.JobDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobDefinitionRepository extends JpaRepository<JobDefinition, Long> {
    @Query("""
        SELECT jd
        FROM JobDefinition jd
        WHERE jd.schedule.scheduleEnabled = :scheduleEnabled
        """)
    List<JobDefinition> findByScheduleEnabled(Boolean scheduleEnabled);

    Optional<JobDefinition> findByMetadataName(String name);

    @Query(value = """
            SELECT *
            FROM job_definitions
            WHERE schedule_enabled = true
              AND next_scheduled_at <= now()
            ORDER BY next_scheduled_at
            FOR UPDATE SKIP LOCKED
            LIMIT 10;
            """, nativeQuery = true)
    List<JobDefinition> findSchedulableJobDefinitions();
}
