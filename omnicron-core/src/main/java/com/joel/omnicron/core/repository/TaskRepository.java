package com.joel.omnicron.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.joel.omnicron.core.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByJobIdOrderByTaskIndexAscAttemptAsc(Long jobId);

    boolean existsByJobIdAndTaskIndexAndAttempt(Long jobId, Integer taskIndex, Integer attempt);
}
