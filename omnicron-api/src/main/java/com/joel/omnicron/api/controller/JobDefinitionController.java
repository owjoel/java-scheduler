package com.joel.omnicron.api.controller;

import com.joel.omnicron.api.dto.ApiResponse;
import com.joel.omnicron.api.dto.CreateJobDefinitionRequest;
import com.joel.omnicron.api.dto.GetJobDefinitionResponse;
import com.joel.omnicron.api.service.JobDefinitionService;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/job-definitions")
public class JobDefinitionController {
    private final JobDefinitionService jobDefinitionService;

    public JobDefinitionController(JobDefinitionService jobDefinitionService) {
        this.jobDefinitionService = jobDefinitionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GetJobDefinitionResponse>>> getJobDefinitions() {
        List<GetJobDefinitionResponse> jobDefinitions = jobDefinitionService.getAllJobDefinitions();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, jobDefinitions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetJobDefinitionResponse>> getJobDefinition(@PathVariable("id") Long id) {
        GetJobDefinitionResponse jobDefinition = jobDefinitionService.getJobDefinition(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, jobDefinition));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createJobDefinition(
            @Valid @RequestBody CreateJobDefinitionRequest request) {
        jobDefinitionService.createJobDefinition(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, null));
    }
}
