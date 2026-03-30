package com.ojasva.manik.relayq.job;

import com.ojasva.manik.relayq.job.dto.CreateJobRequest;
import com.ojasva.manik.relayq.job.dto.GetJobResponse;
import com.ojasva.manik.relayq.job.dto.RescheduleJobRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody CreateJobRequest job) {
        return ResponseEntity.ok(jobService.createJob(job));
    }

    @GetMapping
    public ResponseEntity<Page<GetJobResponse>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(jobService.getJobs(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetJobResponse> getJob(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.deleteJob(id));
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<?> rescheduleJob(@PathVariable UUID id,
                                           @RequestBody(required = false) RescheduleJobRequest request) {
        RescheduleJobRequest effectiveRequest = request != null ? request : new RescheduleJobRequest(Optional.empty());
        return ResponseEntity.ok(jobService.rescheduleJob(id, effectiveRequest));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<?> getJobLogs(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.getJobLogs(id));
    }
}
