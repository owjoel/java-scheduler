package com.joel.java_scheduler.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.joel.java_scheduler.core.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

}
