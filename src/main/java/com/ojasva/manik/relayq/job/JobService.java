package com.ojasva.manik.relayq.job;

import com.ojasva.manik.relayq.common.SecurityUtils;
import com.ojasva.manik.relayq.common.exception.BadRequestException;
import com.ojasva.manik.relayq.common.exception.ResourceNotFoundException;
import com.ojasva.manik.relayq.job.dto.*;
import com.ojasva.manik.relayq.joblog.JobLog;
import com.ojasva.manik.relayq.joblog.JobLogRepository;
import com.ojasva.manik.relayq.queue.Queue;
import com.ojasva.manik.relayq.queue.QueueRepository;
import com.ojasva.manik.relayq.tenant.Tenant;
import com.ojasva.manik.relayq.tenant.TenantRepository;
import com.ojasva.manik.relayq.user.User;
import com.ojasva.manik.relayq.user.UserPrincipal;
import com.ojasva.manik.relayq.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Service
public class JobService {

    private final JobRepository jobRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final QueueRepository queueRepository;
    private final JobLogRepository jobLogRepository;
    private final ObjectMapper objectMapper;
    private final JobMapper jobMapper;

    public JobService(JobRepository jobRepository,
                      TenantRepository tenantRepository,
                      UserRepository userRepository,
                      QueueRepository queueRepository,
                      JobLogRepository jobLogRepository,
                      ObjectMapper objectMapper,
                      JobMapper jobMapper) {
        this.jobRepository = jobRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.queueRepository = queueRepository;
        this.jobLogRepository = jobLogRepository;
        this.objectMapper = objectMapper;
        this.jobMapper = jobMapper;
    }

    @Transactional
    public CreateJobResponse createJob(CreateJobRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();

        Tenant tenant = tenantRepository.findById(principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Queue queue = queueRepository.findByIdAndTenantId(request.queueId(), principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found"));

        Job job = new Job();
        job.setQueue(queue);
        job.setTenant(tenant);
        job.setCreatedBy(user);
        job.setStatus(Job.Status.PENDING);
        job.setRetryCount(0);
        job.setMaxRetries(queue.getMaxRetries());
        request.priority().ifPresent(job::setPriority);
        request.scheduledAt().ifPresent(job::setScheduledAt);

        try {
            job.setPayload(objectMapper.writeValueAsString(request.payload()));
        } catch (Exception e) {
            throw new BadRequestException("Invalid payload format");
        }

        jobRepository.save(job);

        JobLog jobLog = new JobLog();
        jobLog.setJob(job);
        jobLog.setMessage("Job created for queue: " + queue.getName());
        jobLogRepository.save(jobLog);

        return new CreateJobResponse("Job created successfully", true);
    }

    public Page<GetJobResponse> getJobs(int page, int size) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Job> jobs;
        if (principal.getAuthorities().stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"))) {
            jobs = jobRepository.findAllByTenantId(principal.getTenantId(), pageable);
        } else {
            jobs = jobRepository.findAllByTenantIdAndCreatedById(principal.getTenantId(), principal.getId(), pageable);
        }

        return jobs.map(jobMapper::toResponse);
    }

    public GetJobResponse getJob(UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        Job job;
        job = jobRepository.findByIdAndCreatedBy_Id(id, principal.getId()).orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        return jobMapper.toResponse(job);
    }

    public DeleteJobResponse deleteJob(UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        Job job = jobRepository.findByIdAndCreatedBy_Id(id, principal.getId()).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getStatus().equals(Job.Status.PENDING)) {
            throw new BadRequestException("Only PENDING jobs can be cancelled, this job is " + job.getStatus().name().toLowerCase());
        }
        jobRepository.delete(job);
        return new DeleteJobResponse("Job deleted successfully", true);
    }

    public RescheduleJobResponse rescheduleJob(UUID id, RescheduleJobRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();

        Job job = jobRepository.findByIdAndCreatedBy_Id(id, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        switch (job.getStatus()) {
            case PROCESSING -> throw new BadRequestException("Cannot reschedule a job that is currently processing");
            case DONE -> throw new BadRequestException("Cannot reschedule a completed job");
            case FAILED -> {
                job.setStatus(Job.Status.PENDING);
                job.setRetryCount(0);
                job.setErrorMessage(null);
            }
            case PENDING -> {
            }
        }

        job.setScheduledAt(request.scheduledAt().orElse(null));
        jobRepository.save(job);

        return new RescheduleJobResponse("Job rescheduled successfully", true);
    }

    public List<GetJobLogsResponse> getJobLogs(UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        Job job = jobRepository.findByIdAndCreatedBy_Id(id, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        List<JobLog> jobLog = jobLogRepository.findAllByJobIdOrderByCreatedAtAsc(job.getId());
        return jobLog.stream().map(jobMapper::toResponse).toList();
    }
}
