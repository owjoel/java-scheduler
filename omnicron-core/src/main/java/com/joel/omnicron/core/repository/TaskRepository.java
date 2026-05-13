package com.joel.omnicron.core.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.joel.omnicron.core.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query(value = """
            WITH claimed AS (
                SELECT id
                FROM tasks
                WHERE status = 'QUEUED'
                  AND (locked_until IS NULL OR locked_until < now())
                ORDER BY created_at
                FOR UPDATE SKIP LOCKED
                LIMIT :limit
            )
            UPDATE tasks
            SET locked_by = :workerId,
                locked_until = :lockedUntil
            FROM claimed
            WHERE tasks.id = claimed.id
            RETURNING tasks.id
            """, nativeQuery = true)
    List<Long> claimQueuedTasks(
            @Param("workerId") String workerId,
            @Param("lockedUntil") Instant lockedUntil,
            @Param("limit") int limit);

    @Query(value = """
            SELECT id
            FROM tasks
            WHERE status = 'QUEUED'
            ORDER BY created_at
            LIMIT :limit;
            """, nativeQuery = true)
    List<Long> findQueuedTasks(@Param("limit") int limit);

    @Query(value = """
            UPDATE tasks
            SET locked_by = :workerId,
                locked_until = :lockedUntil
            WHERE id = :id
            AND status = 'QUEUED'
            AND (locked_until IS NULL OR locked_until < now())
            RETURNING *;
            """, nativeQuery = true)
    Task claimQueuedTask(
            @Param("id") Long id,
            @Param("workerId") String workerId,
            @Param("lockedUntil") Instant lockedUntil);
}
