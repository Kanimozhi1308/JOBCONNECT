package com.jobportal.jobconnect.controllertests;

import com.jobportal.jobconnect.controller.JobController;
import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.service.JobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobControllerTest {

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    private Job job1;
    private Job job2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        job1 = new Job(1L, "Java Developer", "Develop APIs", "Chennai", "6 LPA",
                LocalDate.now().plusDays(30), 101L);
        job2 = new Job(2L, "Frontend Engineer", "Build React apps", "Bangalore", "7 LPA",
                LocalDate.now().plusDays(45), 102L);
    }

    // ✅ Test: Get all jobs
    @Test
    void testGetAllJobs_Success() {
        List<Job> jobList = Arrays.asList(job1, job2);
        when(jobService.getAllJobs()).thenReturn(jobList);

        List<Job> result = jobController.allJobs();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java Developer", result.get(0).getTitle());
        verify(jobService, times(1)).getAllJobs();
    }

    // ✅ Test: Get job by ID
    @Test
    void testGetJobById_Success() {
        when(jobService.getJobById(1L)).thenReturn(job1);

        Job result = jobController.get(1L);

        assertNotNull(result);
        assertEquals("Java Developer", result.getTitle());
        verify(jobService, times(1)).getJobById(1L);
    }

    // ❌ Test: Get job by ID - Not Found (null)
    @Test
    void testGetJobById_NotFound() {
        when(jobService.getJobById(999L)).thenReturn(null);

        Job result = jobController.get(999L);

        assertNull(result);
        verify(jobService, times(1)).getJobById(999L);
    }

    // ✅ Test: Search jobs (by keyword and location)
    @Test
    void testSearchJobs_WithKeywordAndLocation() {
        when(jobService.searchJobs("Developer", "Chennai")).thenReturn(Arrays.asList(job1));

        List<Job> result = jobController.searchJobs("Developer", "Chennai");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Developer", result.get(0).getTitle());
        verify(jobService, times(1)).searchJobs("Developer", "Chennai");
    }

    // ✅ Test: Search jobs (no filters)
    @Test
    void testSearchJobs_NoFilters() {
        when(jobService.searchJobs(null, null)).thenReturn(Arrays.asList(job1, job2));

        List<Job> result = jobController.searchJobs(null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jobService, times(1)).searchJobs(null, null);
    }

    @Test
    void testDeleteJob_Success() {
        // Arrange: mock successful delete
        doNothing().when(jobService).deleteJobById(1L);

        // Act
        ResponseEntity<String> response = jobController.deleteJob(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("✅ Job deleted successfully", response.getBody());
        verify(jobService, times(1)).deleteJobById(1L);
    }

    @Test
    void testDeleteJob_Failure() {
        // Arrange: mock service to throw exception
        doThrow(new RuntimeException("Job not found"))
                .when(jobService).deleteJobById(99L);

        // Act
        ResponseEntity<String> response = jobController.deleteJob(99L);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("❌ Failed to delete job"));
        verify(jobService, times(1)).deleteJobById(99L);
    }
}
