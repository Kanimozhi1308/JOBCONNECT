package com.jobportal.jobconnect.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jobportal.jobconnect.model.Application;
import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.repository.ApplicationRepository;
import com.jobportal.jobconnect.repository.JobRepository;
import com.jobportal.jobconnect.repository.UserRepository;

@Service
public class ApplicationService {

        @Autowired
        private ApplicationRepository applicationRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private JobRepository jobRepository;

        @Autowired
        private TwilioSMSService smsService;

        @Autowired
        private UserService userService;

        @Autowired
        private JobService jobService;

        /**
         * Applies for a job and saves application record.
         */
        public String applyForJob(Long userId, Long jobId, String name, String email, String phone) {
                // Check if the user already applied for this job
                Optional<Application> existing = applicationRepository.findByUserIdAndJobId(userId, jobId);
                if (existing.isPresent()) {
                        return "You have already applied for this job.";
                }

                // Create new application entry
                Application app = new Application();
                app.setUserId(userId);
                app.setJobId(jobId);
                app.setApplicantName(name);
                app.setApplicantEmail(email);
                app.setApplicantPhone(phone);
                app.setAppliedDate(LocalDateTime.now());

                // Save in database
                applicationRepository.save(app);

                return "Application submitted successfully!";
        }

        /**
         * Updates application status and notifies applicant via SMS.
         */
        public Application updateStatus(Long applicationId, String newStatus, Long employerId) {
                // Find the application record
                Application application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new NoSuchElementException("Application not found"));

                // Find the job linked to that application
                Job job = jobRepository.findById(application.getJobId())
                                .orElseThrow(() -> new NoSuchElementException("Job not found"));

                // Update status
                application.setStatus(newStatus);
                Application updated = applicationRepository.save(application);

                // Notify the applicant
                User user = userRepository.findById(application.getUserId())
                                .orElseThrow(() -> new NoSuchElementException("User not found"));

                String message = "Hi " + user.getFullName() +
                                ", your application for the job '" + job.getTitle() +
                                "' has been updated to: " + newStatus + ".";
                smsService.sendSMS(user.getMobileNumber(), message);

                return updated;
        }

        /** Gets all applications for an employer’s jobs. */
        public List<Application> getApplicationsByEmployer(Long employerId) {
                List<Long> jobIds = jobRepository.findByEmployerId(employerId)
                                .stream()
                                .map(Job::getId)
                                .toList();

                // If no jobs, return empty list
                if (jobIds.isEmpty())
                        return new ArrayList<>();

                // Fetch all applications for these jobs
                return applicationRepository.findByJobIdIn(jobIds);
        }

        /** Finds application by user & job combination. */
        public Optional<Application> findByUserIdAndJobId(Long userId, Long jobId) {
                return applicationRepository.findByUserIdAndJobId(userId, jobId);
        }

        /** Gets all applications submitted by a specific user. */
        public List<Application> getApplicationsByUserId(Long userId) {
                return applicationRepository.findByUserId(userId);
        }

        /** Fetch applicant details for a given ID. */
        public User getApplicantById(Long userId) {
                return userRepository.findById(userId).orElse(null);
        }

}
