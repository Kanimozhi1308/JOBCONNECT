package com.jobportal.jobconnect.controllertests;

import com.jobportal.jobconnect.controller.ApplicationController;
import com.jobportal.jobconnect.model.Application;
import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.service.ApplicationService;
import com.jobportal.jobconnect.service.EmployerJobService;
import com.jobportal.jobconnect.service.JobService;
import com.jobportal.jobconnect.service.TwilioSMSService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private JobService jobService;

    @Mock
    private EmployerJobService employerJobService;

    @Mock
    private TwilioSMSService smsService;

    @InjectMocks
    private ApplicationController applicationController;

    private Application application;
    private Job job;

    @BeforeEach
    void setup() {
        application = new Application();
        application.setId(1L);
        application.setUserId(10L);
        application.setJobId(100L);
        application.setApplicantName("John Doe");
        application.setApplicantEmail("john@example.com");
        application.setApplicantPhone("9876543210");
        application.setAppliedDate(LocalDateTime.now());
        application.setStatus("Pending");

        job = new Job();
        job.setId(100L);
        job.setTitle("Java Developer");
        job.setLocation("Chennai");
    }

    // ✅ Test: applyForJob — user already applied
    @Test
    void testApplyForJob_AlreadyApplied() {
        when(applicationService.findByUserIdAndJobId(10L, 100L))
                .thenReturn(Optional.of(application));

        ResponseEntity<String> response = applicationController.applyForJob(10L, 100L,
                "John Doe", "john@example.com", "9876543210");

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("You have already applied for this job!", response.getBody());
        verify(applicationService, never()).applyForJob(anyLong(), anyLong(), anyString(), anyString(), anyString());
    }

    // ✅ Test: applyForJob — success
    @Test
    void testApplyForJob_Success() {
        when(applicationService.findByUserIdAndJobId(10L, 100L))
                .thenReturn(Optional.empty());

        ResponseEntity<String> response = applicationController.applyForJob(10L, 100L,
                "John Doe", "john@example.com", "9876543210");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Application submitted successfully!", response.getBody());
        verify(applicationService, times(1))
                .applyForJob(10L, 100L, "John Doe", "john@example.com", "9876543210");
    }

    // ✅ Test: getApplicationsByUser — applications found
    @Test
    void testGetApplicationsByUser_Found() {
        List<Application> apps = List.of(application);
        when(applicationService.getApplicationsByUserId(10L)).thenReturn(apps);

        ResponseEntity<?> response = applicationController.getApplicationsByUser(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Application> result = (List<Application>) response.getBody();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getApplicantName());
    }

    // ✅ Test: getApplicationsByUser — not found
    @Test
    void testGetApplicationsByUser_NotFound() {
        when(applicationService.getApplicationsByUserId(10L)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = applicationController.getApplicationsByUser(10L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("message"));
        assertEquals("No applications found for this user.",
                ((Map<?, ?>) response.getBody()).get("message"));
    }

    // ✅ Test: getAppliedJobs — job details fetched correctly
    @Test
    void testGetAppliedJobs_Success() {
        when(applicationService.getApplicationsByUserId(10L))
                .thenReturn(List.of(application));
        when(jobService.getJobById(100L)).thenReturn(job);

        ResponseEntity<List<Map<String, Object>>> response = applicationController.getAppliedJobs(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Map<String, Object>> result = response.getBody();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Developer", result.get(0).get("jobTitle"));
        assertEquals("Pending", result.get(0).get("status"));
    }
}
