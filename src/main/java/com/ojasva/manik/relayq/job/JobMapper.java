package com.ojasva.manik.relayq.job;

import com.ojasva.manik.relayq.job.dto.GetJobLogsResponse;
import com.ojasva.manik.relayq.job.dto.GetJobResponse;
import com.ojasva.manik.relayq.joblog.JobLog;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class JobMapper {

    private final ObjectMapper objectMapper;

    public JobMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GetJobResponse toResponse(Job job) {
        Object payload;
        try {
            payload = objectMapper.readValue(job.getPayload(), Object.class);
        } catch (Exception e) {
            payload = job.getPayload();
        }
        return new GetJobResponse(
                job.getId(),
                job.getStatus(),
                job.getPriority(),
                payload,
                job.getRetryCount(),
                job.getMaxRetries(),
                job.getErrorMessage(),
                job.getScheduledAt(),
                job.getStartedAt(),
                job.getCompletedAt(),
                job.getCreatedAt()
        );
    }

    public GetJobLogsResponse toResponse(JobLog jobLog) {
        return new GetJobLogsResponse(
                jobLog.getMessage(),
                jobLog.getCreatedAt()
        );
    }
}
