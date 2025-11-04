package com.jobportal.jobconnect.servicetests;

import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.repository.ApplicationRepository;
import com.jobportal.jobconnect.repository.JobRepository;
import com.jobportal.jobconnect.repository.UserRepository;
import com.jobportal.jobconnect.service.JobService;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobService jobService;

    @Mock
    private ApplicationRepository applicationRepository;

    private Job job;
    private User employer;

    @BeforeEach
    void setup() {
        employer = new User();
        employer.setId(1L);
        employer.setFullName("Employer Name");

        job = new Job();
        job.setId(100L);
        job.setTitle("Java Developer");
        job.setLocation("Chennai");
        job.setEmployerId(1L);
    }

    // ✅ Test getAllJobs()
    @Test
    void testGetAllJobs() {
        when(jobRepository.findAll()).thenReturn(Arrays.asList(job));

        List<Job> jobs = jobService.getAllJobs();

        assertNotNull(jobs);
        assertEquals(1, jobs.size());
        assertEquals("Java Developer", jobs.get(0).getTitle());
        verify(jobRepository, times(1)).findAll();
    }

    // ✅ Test getJobById() - Success
    @Test
    void testGetJobById_Success() {
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));

        Job foundJob = jobService.getJobById(100L);

        assertNotNull(foundJob);
        assertEquals("Java Developer", foundJob.getTitle());
        verify(jobRepository).findById(100L);
    }

    // ✅ Test getJobById() - Not Found
    @Test
    void testGetJobById_NotFound() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> jobService.getJobById(999L));
    }

    // ✅ Test saveJob() - Success
    @Test
    void testSaveJob_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employer));
        when(jobRepository.save(job)).thenReturn(job);

        Job savedJob = jobService.saveJob(job);

        assertNotNull(savedJob);
        assertEquals("Java Developer", savedJob.getTitle());
        verify(userRepository).findById(1L);
        verify(jobRepository).save(job);
    }

    // ✅ Test saveJob() - Employer Not Found
    @Test
    void testSaveJob_EmployerNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jobService.saveJob(job));
        assertEquals("Employer not found with id: 1", exception.getMessage());
    }

    // ✅ Test searchJobs() - By Keyword
    @Test
    void testSearchJobs_ByKeyword() {
        when(jobRepository.findByTitleContainingIgnoreCase("Java"))
                .thenReturn(Arrays.asList(job));

        List<Job> results = jobService.searchJobs("Java", "");

        assertEquals(1, results.size());
        assertEquals("Java Developer", results.get(0).getTitle());
    }

    // ✅ Test searchJobs() - By Location
    @Test
    void testSearchJobs_ByLocation() {
        when(jobRepository.findByLocationContainingIgnoreCase("Chennai"))
                .thenReturn(Arrays.asList(job));

        List<Job> results = jobService.searchJobs("", "Chennai");

        assertEquals(1, results.size());
        assertEquals("Java Developer", results.get(0).getTitle());
    }

    // ✅ Test searchJobs() - Keyword and Location
    @Test
    void testSearchJobs_ByKeywordAndLocation() {
        when(jobRepository.findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase("Java", "Chennai"))
                .thenReturn(Arrays.asList(job));

        List<Job> results = jobService.searchJobs("Java", "Chennai");

        assertEquals(1, results.size());
        verify(jobRepository).findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase("Java", "Chennai");
    }

    // ✅ Test searchJobs() - No Filters
    @Test
    void testSearchJobs_NoFilters() {
        when(jobRepository.findAll()).thenReturn(Arrays.asList(job));

        List<Job> results = jobService.searchJobs("", "");

        assertEquals(1, results.size());
        verify(jobRepository).findAll();
    }

    @Test
    void testDeleteJob_Success() {
        // Arrange
        Long jobId = 1L;
        doNothing().when(applicationRepository).deleteByJobId(jobId);
        doNothing().when(jobRepository).deleteById(jobId);
        when(jobRepository.existsById(jobId)).thenReturn(true);

        // Act
        jobService.deleteJobById(jobId);

        // Assert
        verify(jobRepository, times(1)).existsById(jobId);
        verify(applicationRepository, times(1)).deleteByJobId(jobId);
        verify(jobRepository, times(1)).deleteById(jobId);
    }

    @Test
    void testDeleteJob_JobNotFound() {
        // Arrange
        Long jobId = 99L;
        when(jobRepository.existsById(jobId)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> jobService.deleteJobById(jobId));

        assertEquals("Job with ID 99 not found", exception.getMessage());
        verify(jobRepository, times(1)).existsById(jobId);
        verify(applicationRepository, never()).deleteByJobId(any());
        verify(jobRepository, never()).deleteById(any());
    }

}
