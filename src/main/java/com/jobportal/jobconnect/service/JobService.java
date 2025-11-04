package com.jobportal.jobconnect.service;

import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.repository.ApplicationRepository;
import com.jobportal.jobconnect.repository.JobRepository;
import com.jobportal.jobconnect.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Fetches all jobs from the database.
     */
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    /**
     * Searches for jobs by keyword and/or location.
     */
    public List<Job> searchJobs(String keyword, String location) {
        if ((keyword == null || keyword.isEmpty()) && (location == null || location.isEmpty())) {
            return jobRepository.findAll();
        } else if (keyword != null && !keyword.isEmpty() && (location == null || location.isEmpty())) {
            return jobRepository.findByTitleContainingIgnoreCase(keyword);
        } else if ((keyword == null || keyword.isEmpty()) && location != null && !location.isEmpty()) {
            return jobRepository.findByLocationContainingIgnoreCase(location);
        } else {
            return jobRepository.findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(keyword, location);
        }
    }

    /**
     * Gets a job by ID, throws exception if not found.
     */
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job not found with id: " + id));
    }

    /**
     * ✅ Saves a job (for employers posting jobs).
     */
    public Job saveJob(Job job) {
        User employer = userRepository.findById(job.getEmployerId())
                .orElseThrow(() -> new RuntimeException("Employer not found with id: " + job.getEmployerId()));
        return jobRepository.save(job);
    }

    @Transactional
    public void deleteJobById(Long jobId) {

        if (!jobRepository.existsById(jobId)) {
            throw new EntityNotFoundException("Job with ID " + jobId + " not found");
        }
        // Delete related applications first
        applicationRepository.deleteByJobId(jobId);

        // Then delete the job itself
        jobRepository.deleteById(jobId);
    }

}
