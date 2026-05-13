package com.joel.omnicron.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.joel.omnicron.core.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
    @Query(value = """
        SELECT *
        FROM jobs
        WHERE status = 'QUEUED'
        ORDER BY created_at
        FOR UPDATE SKIP LOCKED
        LIMIT :limit;
        """, nativeQuery = true)
    List<Job> claimQueuedJobs(@Param("limit") int limit);
}
