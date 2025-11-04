package com.jobportal.jobconnect.controller;

import com.jobportal.jobconnect.model.Application;
import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.service.ApplicationService;
import com.jobportal.jobconnect.service.EmployerJobService;
import com.jobportal.jobconnect.service.JobService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employer/jobs")
@CrossOrigin(origins = "*")
public class EmployerJobController {

    @Autowired
    private EmployerJobService service;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JobService jobService;

    // Add job
    @PostMapping("/add")
    public ResponseEntity<List<Job>> addMultipleJobs(@RequestBody List<Job> jobs) {
        List<Job> savedJobs = service.saveAllJobs(jobs);
        return ResponseEntity.ok(savedJobs);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Job> updateJob(
            @PathVariable Long id,
            @RequestBody Job updatedJob,
            @RequestParam Long employerId) {

        Job job = service.updateJob(id, updatedJob, employerId);
        return ResponseEntity.ok(job);
    }

    // Get jobs by employer ID
    @GetMapping("/my-jobs/{employerId}")
    public ResponseEntity<List<Job>> myJobs(@PathVariable Long employerId) {
        List<Job> jobs = service.getByEmployer(employerId);
        return ResponseEntity.ok(jobs);
    }

    // Update application status
    @PostMapping("/updateStatus")
    public ResponseEntity<Application> updateApplicationStatus(
            @RequestParam Long applicationId,
            @RequestParam String newStatus,
            @RequestParam Long employerId) {

        return ResponseEntity.ok(applicationService.updateStatus(applicationId, newStatus, employerId));
    }

}
