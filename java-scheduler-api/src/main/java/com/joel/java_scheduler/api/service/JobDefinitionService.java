package com.joel.java_scheduler.api.service;

import com.joel.java_scheduler.api.dto.CreateJobDefinitionRequest;
import com.joel.java_scheduler.api.dto.GetJobDefinitionResponse;
import com.joel.java_scheduler.core.entity.JobDefinition;
import com.joel.java_scheduler.core.entity.JobDefinitionMetadata;
import com.joel.java_scheduler.core.repository.JobDefinitionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JobDefinitionService {

    private final JobDefinitionRepository jobDefinitionRepository;

    public JobDefinitionService(JobDefinitionRepository jobDefinitionRepository) {
        this.jobDefinitionRepository = jobDefinitionRepository;
    }

    public List<GetJobDefinitionResponse> getAllJobDefinitions() {
        return jobDefinitionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GetJobDefinitionResponse getJobDefinition(Long id) {
        JobDefinition jobDefinition = jobDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job definition not found: " + id));
        return toResponse(jobDefinition);
    }

    public GetJobDefinitionResponse createJobDefinition(CreateJobDefinitionRequest request) {
        JobDefinitionMetadata metadata = new JobDefinitionMetadata(
                request.metadata().name(),
                request.metadata().author(),
                request.metadata().description());

        JobDefinition jobDefinition = new JobDefinition(
                metadata,
                request.template(),
                request.options(),
                request.cronExpression(),
                request.maxRetries(),
                request.enabled() != null ? request.enabled() : true);
        return toResponse(jobDefinitionRepository.save(jobDefinition));
    }

    public List<GetJobDefinitionResponse> getEnabledJobDefinitions() {
        return jobDefinitionRepository.findByEnabled(true)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private GetJobDefinitionResponse toResponse(JobDefinition jobDefinition) {
        JobDefinitionMetadata metadata = jobDefinition.getMetadata();

        return new GetJobDefinitionResponse(
                jobDefinition.getId(),
                new GetJobDefinitionResponse.Metadata(
                        metadata.getName(),
                        metadata.getAuthor(),
                        metadata.getDescription()),
                jobDefinition.getTemplate(),
                jobDefinition.getOptions(),
                jobDefinition.getCronExpression(),
                jobDefinition.getMaxRetries(),
                jobDefinition.getEnabled(),
                jobDefinition.getCreatedAt(),
                jobDefinition.getUpdatedAt());
    }
}
