package com.ojasva.manik.relayq.job;

import com.ojasva.manik.relayq.joblog.JobLog;
import com.ojasva.manik.relayq.joblog.JobLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class JobProcessor {

    private final JobRepository jobRepository;
    private final JobLogRepository jobLogRepository;
    private final WebhookService webhookService;

    public JobProcessor(JobRepository jobRepository,
                        JobLogRepository jobLogRepository,
                        WebhookService webhookService) {
        this.jobRepository = jobRepository;
        this.jobLogRepository = jobLogRepository;
        this.webhookService = webhookService;
    }

    @Async("jobExecutor")
    @Transactional
    public void process(Job job) {
        job.setStatus(Job.Status.PROCESSING);
        job.setStartedAt(LocalDateTime.now());
        jobRepository.save(job);

        try {
            webhookService.deliver(job);
            job.setStatus(Job.Status.DONE);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            log(job, "Job completed successfully");

        } catch (Exception e) {
            job.setRetryCount(job.getRetryCount() + 1);
            String errorMessage = resolveErrorMessage(e);
            log(job, "Attempt " + job.getRetryCount() + " failed: " + e.getMessage());

            if (job.getRetryCount() >= job.getMaxRetries()) {
                job.setStatus(Job.Status.FAILED);
                job.setErrorMessage(errorMessage);
                log(job, "Job permanently failed after " + job.getRetryCount() + " attempts");
            } else {
                job.setStatus(Job.Status.PENDING);
                job.setScheduledAt(LocalDateTime.now()
                        .plusSeconds((long) Math.pow(2, job.getRetryCount()) * 10));
                log(job, "Retrying in " + (long) Math.pow(2, job.getRetryCount()) * 10 + " seconds");
            }
            jobRepository.save(job);
        }
    }

    private void log(Job job, String message) {
        JobLog jobLog = new JobLog();
        jobLog.setJob(job);
        jobLog.setMessage(message);
        jobLogRepository.save(jobLog);
    }

    private String resolveErrorMessage(Exception e) {
        if (e instanceof org.springframework.web.client.ResourceAccessException) {
            return "Webhook endpoint unreachable";
        }
        if (e instanceof org.springframework.web.client.HttpClientErrorException ex) {
            return "Webhook returned client error: " + ex.getStatusCode();
        }
        if (e instanceof org.springframework.web.client.HttpServerErrorException ex) {
            return "Webhook returned server error: " + ex.getStatusCode();
        }
        if (e instanceof org.springframework.dao.DataAccessException) {
            return "Internal error during job processing";
        }
        return "Unexpected error: " + e.getClass().getSimpleName();
    }
}
