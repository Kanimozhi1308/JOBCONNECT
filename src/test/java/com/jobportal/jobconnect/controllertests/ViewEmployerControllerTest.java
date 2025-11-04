package com.jobportal.jobconnect.controllertests;

import com.jobportal.jobconnect.controller.ViewEmployerController;
import com.jobportal.jobconnect.model.Application;
import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.service.ApplicationService;
import com.jobportal.jobconnect.service.EmployerJobService;
import com.jobportal.jobconnect.service.JobService;
import com.jobportal.jobconnect.service.NotificationService;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ViewEmployerControllerTest {

    @InjectMocks
    private ViewEmployerController controller;

    @Mock
    private EmployerJobService employerJobService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private JobService jobService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Dashboard test (success)
    @Test
    void testDashboard_Success() {
        Long employerId = 1L;
        when(session.getAttribute("userId")).thenReturn(employerId);

        List<Job> jobs = Collections.singletonList(new Job());
        when(employerJobService.getByEmployer(employerId)).thenReturn(jobs);

        Application app = new Application();
        app.setId(10L);
        app.setJobId(5L);
        app.setUserId(7L);
        app.setStatus("Pending");
        app.setApplicantName("John");
        app.setApplicantEmail("john@example.com");
        app.setApplicantPhone("1234567890");

        when(applicationService.getApplicationsByEmployer(employerId))
                .thenReturn(Collections.singletonList(app));

        Job job = new Job();
        job.setId(5L);
        job.setTitle("Software Engineer");
        when(jobService.getJobById(5L)).thenReturn(job);

        User user = new User();
        user.setId(7L);
        user.setFullName("John Doe");
        when(applicationService.getApplicantById(7L)).thenReturn(user);

        String view = controller.dashboard(session, model);

        assertEquals("employer", view);
        verify(model).addAttribute(eq("employerId"), eq(employerId));
        verify(model).addAttribute(eq("jobs"), eq(jobs));
        verify(model).addAttribute(eq("applications"), anyList());
    }

    // ✅ Dashboard redirect when not logged in
    @Test
    void testDashboard_RedirectToLogin() {
        when(session.getAttribute("userId")).thenReturn(null);
        String view = controller.dashboard(session, model);
        assertEquals("redirect:/login", view);
    }

    // ✅ Save Job
    @Test
    void testSaveJob_Success() {
        Long employerId = 2L;
        Job job = new Job();
        when(session.getAttribute("userId")).thenReturn(employerId);

        String result = controller.saveJob(job, session);

        assertEquals("redirect:/employer/dashboard", result);
        verify(employerJobService).saveAllJobs(anyList());
    }

    // ✅ Update Job
    @Test
    void testUpdateJob_Success() {
        Long employerId = 2L;
        Job updatedJob = new Job();
        when(session.getAttribute("userId")).thenReturn(employerId);

        String result = controller.updateJob(1L, updatedJob, session);

        assertEquals("redirect:/employer/dashboard", result);
        verify(employerJobService).updateJob(1L, updatedJob, employerId);
    }

    // ✅ Update Application Status
    @Test
    void testUpdateApplicationStatus_Success() {
        Long employerId = 5L;
        Application app = new Application();
        when(session.getAttribute("userId")).thenReturn(employerId);
        when(applicationService.updateStatus(1L, "Approved", employerId)).thenReturn(app);

        String result = controller.updateApplicationStatus(1L, "Approved", session);

        assertEquals("redirect:/employer/dashboard", result);
        verify(applicationService).updateStatus(1L, "Approved", employerId);
    }
}
