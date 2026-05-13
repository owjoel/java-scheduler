package com.joel.omnicron.api.service;

import com.joel.omnicron.api.dto.CreateJobDefinitionRequest;
import com.joel.omnicron.api.dto.GetJobDefinitionResponse;
import com.joel.omnicron.core.entity.JobDefinition;
import com.joel.omnicron.core.entity.JobDefinitionMetadata;
import com.joel.omnicron.core.entity.JobDefinitionSchedule;
import com.joel.omnicron.core.entity.TemplateOptionDefinition;
import com.joel.omnicron.core.repository.JobDefinitionRepository;
import com.joel.omnicron.core.util.ScheduleUtils;
import java.time.Instant;
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
        JobDefinitionSchedule schedule = makeSchedule(request.schedule());

        JobDefinition jobDefinition = new JobDefinition(
                metadata,
                request.template(),
                toTemplateOptionDefinitions(request.options()),
                request.maxRetries(),
                request.fanOutSpec(),
                schedule);
        return toResponse(jobDefinitionRepository.save(jobDefinition));
    }

    private List<TemplateOptionDefinition> toTemplateOptionDefinitions(
            List<CreateJobDefinitionRequest.TemplateOption> options) {
        if (options == null) {
            return null;
        }

        return options.stream()
                .map(option -> new TemplateOptionDefinition(
                        option.name(),
                        option.type(),
                        option.required(),
                        option.defaultValue()))
                .toList();
    }

    private JobDefinitionSchedule makeSchedule(CreateJobDefinitionRequest.Schedule dto) {
        if (dto == null) {
            return new JobDefinitionSchedule(null, true, null, null);
        }

        Instant nextScheduledAt = dto.scheduleEnabled() != null && dto.scheduleEnabled()
                ? ScheduleUtils.nextScheduledAt(dto.cronExpression(), Instant.now())
                : null;

        return new JobDefinitionSchedule(
                dto.cronExpression(),
                dto.scheduleEnabled(),
                null,
                nextScheduledAt);
    }

    public List<GetJobDefinitionResponse> getScheduleEnabledJobDefinitions() {
        return jobDefinitionRepository.findByScheduleEnabled(true)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private GetJobDefinitionResponse toResponse(JobDefinition jobDefinition) {
        JobDefinitionMetadata metadata = jobDefinition.getMetadata();
        JobDefinitionSchedule schedule = jobDefinition.getSchedule() != null
                ? jobDefinition.getSchedule()
                : new JobDefinitionSchedule(null, true, null, null);

        return new GetJobDefinitionResponse(
                jobDefinition.getId(),
                new GetJobDefinitionResponse.Metadata(
                        metadata.getName(),
                        metadata.getAuthor(),
                        metadata.getDescription()),
                jobDefinition.getTemplate(),
                jobDefinition.getOptions(),
                jobDefinition.getMaxRetries(),
                jobDefinition.getFanOutSpec(),
                new GetJobDefinitionResponse.Schedule(
                        schedule.getCronExpression(),
                        schedule.getScheduleEnabled(),
                        schedule.getLastScheduledAt(),
                        schedule.getNextScheduledAt()),
                jobDefinition.getCreatedAt(),
                jobDefinition.getUpdatedAt());
    }
}
