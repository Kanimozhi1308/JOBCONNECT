package com.jobportal.jobconnect.controllertests;

import com.jobportal.jobconnect.controller.EmployerJobController;
import com.jobportal.jobconnect.model.Application;
import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.service.ApplicationService;
import com.jobportal.jobconnect.service.EmployerJobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EmployerJobControllerTest {

    @Mock
    private EmployerJobService employerJobService;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private EmployerJobController employerJobController;

    private Job job1;
    private Job job2;
    private Application application;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        job1 = new Job(1L, "Java Developer", "Develop APIs", "Chennai", "6 LPA",
                LocalDate.now().plusDays(30), 101L);
        job2 = new Job(2L, "Frontend Engineer", "React Projects", "Bangalore", "7 LPA",
                LocalDate.now().plusDays(45), 101L);

        application = new Application();
        application.setId(1L);
        application.setJobId(1L);
        application.setUserId(201L);
        application.setApplicantName("John Doe");
        application.setApplicantEmail("john@example.com");
        application.setApplicantPhone("9876543210");
        application.setStatus("Accepted");
    }

    // ✅ Test: Add Multiple Jobs
    @Test
    void testAddMultipleJobs_Success() {
        List<Job> jobList = Arrays.asList(job1, job2);
        when(employerJobService.saveAllJobs(jobList)).thenReturn(jobList);

        ResponseEntity<List<Job>> response = employerJobController.addMultipleJobs(jobList);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(employerJobService, times(1)).saveAllJobs(jobList);
    }

    // ✅ Test: Update Job
    @Test
    void testUpdateJob_Success() {
        Job updatedJob = new Job(1L, "Senior Java Developer", "Develop microservices", "Chennai", "10 LPA",
                LocalDate.now().plusDays(60), 101L);

        when(employerJobService.updateJob(eq(1L), any(Job.class), eq(101L))).thenReturn(updatedJob);

        ResponseEntity<Job> response = employerJobController.updateJob(1L, updatedJob, 101L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Senior Java Developer", response.getBody().getTitle());
        verify(employerJobService, times(1)).updateJob(1L, updatedJob, 101L);
    }

    // ✅ Test: Get Jobs by Employer
    @Test
    void testGetJobsByEmployer_Success() {
        List<Job> jobs = Arrays.asList(job1, job2);
        when(employerJobService.getByEmployer(101L)).thenReturn(jobs);

        ResponseEntity<List<Job>> response = employerJobController.myJobs(101L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertEquals("Java Developer", response.getBody().get(0).getTitle());
        verify(employerJobService, times(1)).getByEmployer(101L);
    }

    // ✅ Test: Update Application Status (Success)
    @Test
    void testUpdateApplicationStatus_Success() {
        when(applicationService.updateStatus(1L, "Accepted", 101L)).thenReturn(application);

        ResponseEntity<Application> response = employerJobController.updateApplicationStatus(1L, "Accepted", 101L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Accepted", response.getBody().getStatus());
        verify(applicationService, times(1)).updateStatus(1L, "Accepted", 101L);
    }
}