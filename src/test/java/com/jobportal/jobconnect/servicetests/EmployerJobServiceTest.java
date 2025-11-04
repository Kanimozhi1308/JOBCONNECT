package com.jobportal.jobconnect.servicetests;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;

import com.jobportal.jobconnect.model.*;
import com.jobportal.jobconnect.repository.*;
import com.jobportal.jobconnect.service.*;

@ExtendWith(MockitoExtension.class)
class EmployerJobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private TwilioSMSService smsService;

    @InjectMocks
    private EmployerJobService employerJobService;

    private Job job;
    private User employer;
    private Application application;

    @BeforeEach
    void setup() {
        employer = new User();
        employer.setId(1L);
        employer.setEmail("employer@example.com");
        employer.setFullName("John Employer");

        job = new Job();
        job.setId(100L);
        job.setEmployerId(1L);
        job.setTitle("Java Developer");
        job.setDescription("Spring Boot Developer");
        job.setLocation("Chennai");
        job.setSalary("8 LPA");
        job.setDeadline(LocalDate.now().plusDays(10));

        application = new Application();
        application.setId(200L);
        application.setJobId(100L);
        application.setUserId(2L);
        application.setStatus("Pending");
    }

    // ✅ Test saveAllJobs
    @Test
    void testSaveAllJobs() {
        List<Job> jobList = List.of(job);
        when(jobRepository.saveAll(jobList)).thenReturn(jobList);

        List<Job> result = employerJobService.saveAllJobs(jobList);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(jobRepository, times(1)).saveAll(jobList);
    }

    // ✅ Test updateJob (Success)
    @Test
    void testUpdateJob_Success() {
        Job updatedJob = new Job();
        updatedJob.setTitle("Senior Java Developer");
        updatedJob.setDescription("Updated desc");
        updatedJob.setLocation("Bangalore");
        updatedJob.setSalary("12 LPA");
        updatedJob.setDeadline(LocalDate.now().plusDays(20));

        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenReturn(updatedJob);

        Job result = employerJobService.updateJob(100L, updatedJob, 1L);

        assertEquals("Senior Java Developer", result.getTitle());
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    // ❌ Test updateJob (Unauthorized)
    @Test
    void testUpdateJob_Unauthorized() {
        Job updatedJob = new Job();
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));

        assertThrows(IllegalArgumentException.class, () -> employerJobService.updateJob(100L, updatedJob, 999L));
    }

    // ✅ Test deleteJob (Success)
    // @Test
    // void testDeleteJob_Success() {
    // when(jobRepository.findById(100L)).thenReturn(Optional.of(job));

    // employerJobService.deleteJob(100L);

    // verify(jobRepository, times(1)).delete(job);
    // }

    // // ❌ Test deleteJob (Job not found)
    // @Test
    // void testDeleteJob_NotFound() {
    // when(jobRepository.findById(999L)).thenReturn(Optional.empty());

    // assertThrows(NoSuchElementException.class, () ->
    // employerJobService.deleteJob(999L));
    // }

    // ✅ Test getEmployerIdByEmail
    @Test
    void testGetEmployerIdByEmail() {
        when(userRepository.findByEmail("employer@example.com")).thenReturn(Optional.of(employer));

        Long employerId = employerJobService.getEmployerIdByEmail("employer@example.com");

        assertEquals(1L, employerId);
    }

    // ❌ Test getEmployerIdByEmail (Not found)
    @Test
    void testGetEmployerIdByEmail_NotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> employerJobService.getEmployerIdByEmail("missing@example.com"));
    }

    // ✅ Test getByEmployer
    @Test
    void testGetByEmployer() {
        List<Job> jobList = List.of(job);
        when(jobRepository.findByEmployerId(1L)).thenReturn(jobList);

        List<Job> result = employerJobService.getByEmployer(1L);

        assertEquals(1, result.size());
        assertEquals("Java Developer", result.get(0).getTitle());
    }
}
