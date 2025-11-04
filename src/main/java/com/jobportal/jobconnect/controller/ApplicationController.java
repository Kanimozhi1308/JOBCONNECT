package com.jobportal.jobconnect.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jobportal.jobconnect.model.Application;
import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.service.ApplicationService;
import com.jobportal.jobconnect.service.EmployerJobService;
import com.jobportal.jobconnect.service.JobService;
import com.jobportal.jobconnect.service.TwilioSMSService;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JobService jobService;

    @Autowired
    private EmployerJobService service;

    @Autowired
    private TwilioSMSService smsService;

    @PostMapping("/apply")
    public ResponseEntity<String> applyForJob(
            @RequestParam Long userId,
            @RequestParam Long jobId,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone) {

        Optional<Application> existing = applicationService.findByUserIdAndJobId(userId, jobId);
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("You have already applied for this job!");
        }

        applicationService.applyForJob(userId, jobId, name, email, phone);
        return ResponseEntity.ok("Application submitted successfully!");
    }

    // NEW — View all applications by user ID (for job seeker)
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getApplicationsByUser(@PathVariable Long userId) {
        List<Application> applications = applicationService.getApplicationsByUserId(userId);

        if (applications == null || applications.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No applications found for this user."));
        }

        return ResponseEntity.ok(applications);
    }

    // Get applied jobs for a specific user
    @GetMapping("/user/{userId}/applied-jobs")
    public ResponseEntity<List<Map<String, Object>>> getAppliedJobs(@PathVariable Long userId) {
        List<Application> applications = applicationService.getApplicationsByUserId(userId);
        List<Map<String, Object>> appliedJobs = new ArrayList<>();

        for (Application app : applications) {
            Job job = jobService.getJobById(app.getJobId());
            if (job != null) {
                Map<String, Object> jobData = new HashMap<>();
                jobData.put("jobTitle", job.getTitle());
                jobData.put("jobLocation", job.getLocation());
                jobData.put("status", app.getStatus());
                jobData.put("appliedDate", app.getAppliedDate());
                appliedJobs.add(jobData);
            }
        }
        return ResponseEntity.ok(appliedJobs);
    }

}
