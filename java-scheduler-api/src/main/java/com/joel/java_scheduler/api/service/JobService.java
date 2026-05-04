package com.joel.java_scheduler.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.joel.java_scheduler.api.dto.GetJobResponse;
import com.joel.java_scheduler.core.entity.Job;
import com.joel.java_scheduler.core.entity.JobMetadata;
import com.joel.java_scheduler.core.entity.JobState;
import com.joel.java_scheduler.core.repository.JobRepository;

@Service
public class JobService {
	private final JobRepository jobRepository;

	public JobService(JobRepository jobRepository) {
		this.jobRepository = jobRepository;
	}

	public List<GetJobResponse> getAllJobs() {
		return jobRepository.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	public GetJobResponse getJob(Long id) {
		Job job = jobRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
		return toResponse(job);
	}

	private GetJobResponse toResponse(Job job) {
		JobMetadata metadata = job.getMetadata();
		JobState state = job.getState();

		return new GetJobResponse(
				job.getId(),
				job.getJobDefinitionId(),
				new GetJobResponse.Metadata(metadata.getName(), metadata.getOperator()),
				job.getJobType(),
				job.getOptionValues(),
				new GetJobResponse.State(
						state.getStatus(),
						state.getFailureMessage(),
						state.getStart(),
						state.getEnd()),
				job.getCreatedAt(),
				job.getUpdatedAt());
	}
}
