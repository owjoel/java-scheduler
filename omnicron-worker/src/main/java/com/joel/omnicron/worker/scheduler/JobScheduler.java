package com.joel.omnicron.worker.scheduler;

import com.joel.omnicron.core.entity.Job;
import com.joel.omnicron.core.entity.JobDefinition;
import com.joel.omnicron.core.entity.JobDefinitionMetadata;
import com.joel.omnicron.core.entity.JobDefinitionSchedule;
import com.joel.omnicron.core.entity.JobMetadata;
import com.joel.omnicron.core.entity.JobState;
import com.joel.omnicron.core.entity.JobStatus;
import com.joel.omnicron.core.entity.JobType;
import com.joel.omnicron.core.entity.TemplateOptionDefinition;
import com.joel.omnicron.core.repository.JobDefinitionRepository;
import com.joel.omnicron.core.repository.JobRepository;
import com.joel.omnicron.core.util.ScheduleUtils;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JobScheduler {
    private final JobDefinitionRepository jobDefinitionRepository;
    private final JobRepository jobRepository;

    public JobScheduler(
            JobDefinitionRepository jobDefinitionRepository,
            JobRepository jobRepository) {
        this.jobDefinitionRepository = jobDefinitionRepository;
        this.jobRepository = jobRepository;
    }

    @Scheduled(fixedDelayString = "${worker.scheduler.interval:1000}")
    @Transactional
    public void scheduleJobs() {
        // Get schedulable jobs
        List<JobDefinition> jobDefinitions = jobDefinitionRepository.findSchedulableJobDefinitions();

        for (JobDefinition jobDefinition : jobDefinitions) {
            JobDefinitionMetadata metadata = jobDefinition.getMetadata();
            JobDefinitionSchedule schedule = jobDefinition.getSchedule();

            // Create new job in DB
            Job job = new Job(
                    jobDefinition.getId(),
                    new JobMetadata(metadata.getName(), "scheduler"),
                    JobType.CRON,
                    resolveOptionValues(jobDefinition),
                    jobDefinition.getTemplate(),
                    jobDefinition.getFanOutSpec(),
                    new JobState(JobStatus.QUEUED, null, null, null));
            jobRepository.save(job);

            // Update schedule in job definition
            Instant scheduledAt = schedule.getNextScheduledAt();
            Instant nextScheduledAt = ScheduleUtils.nextScheduledAt(schedule.getCronExpression(), scheduledAt);
            schedule.setLastScheduledAt(scheduledAt);
            schedule.setNextScheduledAt(nextScheduledAt);
            jobDefinitionRepository.save(jobDefinition);
        }
    }

    private Map<String, Object> resolveOptionValues(JobDefinition jobDefinition) {
        Map<String, Object> optionValues = new HashMap<>();

        if (jobDefinition.getOptions() == null) {
            return optionValues;
        }

        for (TemplateOptionDefinition option : jobDefinition.getOptions()) {
            Object value = option.defaultValue();
            boolean required = option.required() != null && option.required();

            if ((value == null || value instanceof String stringValue && stringValue.isBlank()) && required) {
                throw new IllegalArgumentException("Required option has no default: " + option.name());
            }

            if (value != null) {
                optionValues.put(option.name(), coerceOptionValue(option, value));
            }
        }

        return optionValues;
    }

    private Object coerceOptionValue(TemplateOptionDefinition option, Object value) {
        return switch (option.type()) {
            case INTEGER -> coerceInteger(option, value);
            case BOOLEAN -> coerceBoolean(option, value);
            default -> value.toString();
        };
    }

    private Integer coerceInteger(TemplateOptionDefinition option, Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Option must be an integer: " + option.name());
        }
    }

    private Boolean coerceBoolean(TemplateOptionDefinition option, Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        String text = value.toString().trim().toLowerCase();
        if (text.equals("true")) {
            return true;
        }

        if (text.equals("false")) {
            return false;
        }

        throw new IllegalArgumentException("Option must be a boolean: " + option.name());
    }
}
