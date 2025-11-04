package com.jobportal.jobconnect.controllertests;

import com.jobportal.jobconnect.controller.ViewJobSeekerController;
import com.jobportal.jobconnect.model.Application;
import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.service.ApplicationService;
import com.jobportal.jobconnect.service.JobService;
import com.jobportal.jobconnect.service.NotificationService;
import com.jobportal.jobconnect.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ViewJobSeekerControllerTest {

    @InjectMocks
    private ViewJobSeekerController controller;

    @Mock
    private JobService jobService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Redirect to dashboard (success)
    @Test
    void testRedirectToDashboard_Success() {
        User user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setMobileNumber("9999999999");

        when(userService.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        String view = controller.redirectToDashboard("john@example.com", session);

        assertEquals("redirect:/jobseeker/dashboard", view);
        verify(session).setAttribute("userId", 1L);
        verify(session).setAttribute("userName", "John Doe");
        verify(session).setAttribute("userEmail", "john@example.com");
        verify(session).setAttribute("userPhone", "9999999999");
    }

    // ✅ Redirect to login when email not found
    @Test
    void testRedirectToDashboard_UserNotFound() {
        when(userService.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        String view = controller.redirectToDashboard("unknown@example.com", session);
        assertEquals("redirect:/login", view);
    }

    @Test
    void testDashboard_Success() {
        // Mock dependencies
        HttpSession session = mock(HttpSession.class);
        Model model = mock(Model.class);

        // ✅ Simulate a logged-in user
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getAttribute("userName")).thenReturn("John Doe");
        when(session.getAttribute("userEmail")).thenReturn("john@example.com");
        when(session.getAttribute("userPhone")).thenReturn("9876543210");

        // ✅ Mock job data
        List<Job> jobs = List.of(new Job());
        when(jobService.searchJobs(null, null)).thenReturn(jobs);
        when(applicationService.getApplicationsByUserId(1L)).thenReturn(new ArrayList<>());
        when(jobService.getAllJobs()).thenReturn(jobs);

        // ✅ Execute
        String viewName = controller.jobSeekerHome(session, model, null, null);

        // ✅ Verify
        assertEquals("jobseeker", viewName);
        verify(model, atLeastOnce()).addAttribute(eq("jobs"), anyList());
        verify(model, times(1)).addAttribute(eq("applications"), anyList());
    }

    // ✅ Dashboard redirect when session expired
    @Test
    void testDashboard_SessionExpired() {
        when(session.getAttribute("userId")).thenReturn(null);
        String view = controller.jobSeekerHome(session, model, null, null);
        assertEquals("redirect:/login", view);
    }

    // ✅ Apply for job (new application)
    @Test
    void testApplyForJob_Success() {
        when(applicationService.findByUserIdAndJobId(1L, 2L))
                .thenReturn(Optional.empty());

        String view = controller.applyForJob(1L, 2L, "John", "john@example.com", "99999", redirectAttributes);

        assertEquals("redirect:/jobseeker/dashboard", view);
        verify(applicationService).applyForJob(1L, 2L, "John", "john@example.com", "99999");
        verify(redirectAttributes).addFlashAttribute("successMessage", "Application submitted successfully!");
    }

    // ✅ Apply for job (already applied)
    @Test
    void testApplyForJob_AlreadyApplied() {
        Application app = new Application();
        when(applicationService.findByUserIdAndJobId(1L, 2L))
                .thenReturn(Optional.of(app));

        String view = controller.applyForJob(1L, 2L, "John", "john@example.com", "99999", redirectAttributes);

        assertEquals("redirect:/jobseeker/dashboard", view);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "You have already applied for this job!");
        verify(applicationService, never()).applyForJob(anyLong(), anyLong(), anyString(), anyString(), anyString());
    }

    // ✅ Search Jobs
    @Test
    void testSearchJobs_Success() {
        when(session.getAttribute("userId")).thenReturn(1L);
        List<Job> jobs = List.of(new Job());
        when(jobService.searchJobs("Java", "Chennai")).thenReturn(jobs);

        String view = controller.searchJobs("Java", "Chennai", session, model);

        assertEquals("jobseeker", view);
        verify(model).addAttribute("jobs", jobs);
        verify(model).addAttribute("userId", 1L);
    }

    // ✅ Search Jobs when session expired
    @Test
    void testSearchJobs_SessionExpired() {
        when(session.getAttribute("userId")).thenReturn(null);
        String view = controller.searchJobs("Java", "Chennai", session, model);
        assertEquals("redirect:/login", view);
    }

    // ✅ View Applied Jobs
    @Test
    void testViewAppliedJobs_Success() {
        when(session.getAttribute("userId")).thenReturn(1L);

        Job job = new Job();
        job.setId(2L);
        job.setTitle("Software Engineer");
        job.setLocation("Chennai");

        Application app = new Application();
        app.setJobId(2L);
        app.setId(5L);

        when(jobService.getAllJobs()).thenReturn(List.of(job));
        when(applicationService.getApplicationsByUserId(1L)).thenReturn(List.of(app));
        when(jobService.getJobById(2L)).thenReturn(job);

        String view = controller.viewAppliedJobs(session, model);

        assertEquals("redirect:/jobseeker/dashboard", view);
        verify(model).addAttribute(eq("jobs"), anyList());
        verify(model).addAttribute(eq("applications"), anyList());
        verify(model).addAttribute("userId", 1L);
    }

    // ✅ View Applied Jobs when session expired
    @Test
    void testViewAppliedJobs_SessionExpired() {
        when(session.getAttribute("userId")).thenReturn(null);
        String view = controller.viewAppliedJobs(session, model);
        assertEquals("redirect:/login", view);
    }
}
