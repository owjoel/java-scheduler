package com.joel.java_scheduler.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.joel.java_scheduler.api.dto.ApiResponse;
import com.joel.java_scheduler.api.dto.GetJobResponse;
import com.joel.java_scheduler.api.service.JobService;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GetJobResponse>>> getJobs() {
        List<GetJobResponse> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetJobResponse>> getJob(@PathVariable("id") Long id) {
        GetJobResponse job = jobService.getJob(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, job));
    }
}
