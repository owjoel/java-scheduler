package com.joel.omnicron.core.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.joel.omnicron.core.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
    @Query(value = """
            WITH claimed AS (
                SELECT id
                FROM jobs
                WHERE status IN ('QUEUED', 'RUNNING', 'COMPLETING', 'FAILING')
                AND (locked_until IS NULL OR locked_until < now())
                ORDER BY created_at
                FOR UPDATE SKIP LOCKED
                LIMIT :limit
            )
            UPDATE jobs
            SET locked_by = :workerId,
                locked_until = :lockedUntil
            FROM claimed
            WHERE jobs.id = claimed.id
            RETURNING jobs.id
            """, nativeQuery = true)
    List<Long> claimJobs(
            @Param("workerId") String workerId,
            @Param("lockedUntil") Instant lockedUntil,
            @Param("limit") int limit);
}
