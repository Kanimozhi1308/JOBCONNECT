package com.jobportal.jobconnect.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.service.JobService;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping
    public List<Job> allJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/{id}")
    public Job get(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    @GetMapping("/search")
    public List<Job> searchJobs(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {
        return jobService.searchJobs(keyword, location);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        try {
            jobService.deleteJobById(id);
            return ResponseEntity.ok("✅ Job deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to delete job: " + e.getMessage());
        }
    }

}