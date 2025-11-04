package com.jobportal.jobconnect.servicetests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.jobportal.jobconnect.model.*;
import com.jobportal.jobconnect.repository.*;
import com.jobportal.jobconnect.service.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private TwilioSMSService smsService;

    @Mock
    private UserService userService;

    @Mock
    private JobService jobService;

    @InjectMocks
    private ApplicationService applicationService;

    private Application application;
    private Job job;
    private User user;

    @BeforeEach
    void setUp() {
        application = new Application();
        application.setId(1L);
        application.setUserId(1L);
        application.setJobId(1L);
        application.setStatus("Pending");

        job = new Job();
        job.setId(1L);
        job.setEmployerId(10L);
        job.setTitle("Java Developer");

        user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setMobileNumber("9876543210");
    }

    // ✅ 1. applyForJob - new application
    @Test
    void testApplyForJob_NewApplication() {
        when(applicationRepository.findByUserIdAndJobId(1L, 1L)).thenReturn(Optional.empty());
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        String response = applicationService.applyForJob(1L, 1L, "John", "john@example.com", "9876543210");

        assertEquals("Application submitted successfully!", response);
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    // ✅ 2. applyForJob - already applied
    @Test
    void testApplyForJob_AlreadyApplied() {
        when(applicationRepository.findByUserIdAndJobId(1L, 1L)).thenReturn(Optional.of(application));

        String response = applicationService.applyForJob(1L, 1L, "John", "john@example.com", "9876543210");

        assertEquals("You have already applied for this job.", response);
        verify(applicationRepository, never()).save(any());
    }

    // ✅ 3. updateStatus - valid update
    @Test
    void testUpdateStatus_Success() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        Application updated = applicationService.updateStatus(1L, "Accepted", 10L);

        assertNotNull(updated);
        assertEquals("Accepted", updated.getStatus());
        verify(smsService, times(1)).sendSMS(eq("9876543210"), contains("Accepted"));
    }

    // ✅ 5. getApplicationsByEmployer - has jobs
    @Test
    void testGetApplicationsByEmployer_WithJobs() {
        when(jobRepository.findByEmployerId(10L)).thenReturn(List.of(job));
        when(applicationRepository.findByJobIdIn(List.of(1L))).thenReturn(List.of(application));

        List<Application> apps = applicationService.getApplicationsByEmployer(10L);

        assertEquals(1, apps.size());
    }

    // ✅ 6. getApplicationsByEmployer - no jobs
    @Test
    void testGetApplicationsByEmployer_NoJobs() {
        when(jobRepository.findByEmployerId(10L)).thenReturn(Collections.emptyList());

        List<Application> apps = applicationService.getApplicationsByEmployer(10L);

        assertTrue(apps.isEmpty());
    }

    // ✅ 7. findByUserIdAndJobId
    @Test
    void testFindByUserIdAndJobId() {
        when(applicationRepository.findByUserIdAndJobId(1L, 1L)).thenReturn(Optional.of(application));

        Optional<Application> result = applicationService.findByUserIdAndJobId(1L, 1L);

        assertTrue(result.isPresent());
    }

    // ✅ 8. getApplicationsByUserId
    @Test
    void testGetApplicationsByUserId() {
        when(applicationRepository.findByUserId(1L)).thenReturn(List.of(application));

        List<Application> apps = applicationService.getApplicationsByUserId(1L);

        assertEquals(1, apps.size());
    }

    // ✅ 9. getApplicantById - found
    @Test
    void testGetApplicantById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = applicationService.getApplicantById(1L);

        assertNotNull(found);
        assertEquals("John Doe", found.getFullName());
    }

    // ✅ 10. getApplicantById - not found
    @Test
    void testGetApplicantById_NotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        User result = applicationService.getApplicantById(2L);

        assertNull(result);
    }
}
