package com.ojasva.manik.relayq.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class JobPoller {

    private static final Logger logger = LoggerFactory.getLogger(JobPoller.class);
    private final JobRepository jobRepository;
    private final JobProcessor jobProcessor;

    public JobPoller(JobRepository jobRepository, JobProcessor jobProcessor) {
        this.jobRepository = jobRepository;
        this.jobProcessor = jobProcessor;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void poll() {
        Optional<Job> nextJob = jobRepository.findNextPendingJob();
        if (nextJob.isEmpty()) {
            logger.debug("No pending jobs found");
            return;
        }
        nextJob.ifPresent(job -> {
            job.setStatus(Job.Status.PROCESSING);
            job.setStartedAt(LocalDateTime.now());
            jobRepository.save(job);
            jobProcessor.process(job);
        });
    }
}
