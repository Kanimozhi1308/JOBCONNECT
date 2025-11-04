package com.jobportal.jobconnect.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.model.Role;
import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.repository.ApplicationRepository;
import com.jobportal.jobconnect.repository.JobRepository;
import com.jobportal.jobconnect.repository.UserRepository;

@Service
public class EmployerJobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwilioSMSService smsService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Saves jobs and notifies all job seekers via SMS.
     */
    public List<Job> saveAllJobs(List<Job> jobs) {

        // Save all jobs to the database
        List<Job> savedJobs = jobRepository.saveAll(jobs);

        // Fetch only users who are registered as JOB_SEEKER
        List<User> jobSeekers = userRepository.findByUserType(Role.JOB_SEEKER);

        // Iterate through each saved job and notify all valid job seekers
        for (Job job : savedJobs) {
            for (User user : jobSeekers) {

                // 📞 Check if user's phone number is valid before sending SMS
                String phoneNumber = user.getMobileNumber();
                if (isValidPhoneNumber(phoneNumber))
                    try {
                        notificationService.sendJobAlert(user, job.getTitle());
                    } catch (Exception e) {
                        System.out.println("⚠️ Failed to send SMS to " + phoneNumber + ": " + e.getMessage());
                    }
                else {
                    System.out.println("⚠️ Invalid phone number skipped: " + phoneNumber);
                }
            }
        }
        return savedJobs;
    }

    /**
     * Validates phone number format (must be 10 digits and start with 6-9)
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^[6-9]\\d{9}$");
    }

    /** Updates an existing job posted by employer. */
    public Job updateJob(Long id, Job updatedJob, Long employerId) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job not found"));

        if (!job.getEmployerId().equals(employerId)) {
            throw new IllegalArgumentException("You are not authorized to update this job");
        }

        job.setTitle(updatedJob.getTitle());
        job.setDescription(updatedJob.getDescription());
        job.setLocation(updatedJob.getLocation());
        job.setSalary(updatedJob.getSalary());
        job.setDeadline(updatedJob.getDeadline());

        return jobRepository.save(job);
    }

    /** Gets all jobs posted by an employer. */
    public Long getEmployerIdByEmail(String email) {
        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Employer not found"));
        return employer.getId();
    }

    // Get jobs by employer ID
    public List<Job> getByEmployer(Long employerId) {
        return jobRepository.findByEmployerId(employerId);
    }

}
