package com.jobportal.jobconnect.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.jobconnect.model.Application;
import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.service.ApplicationService;
import com.jobportal.jobconnect.service.JobService;
import com.jobportal.jobconnect.service.NotificationService;
import com.jobportal.jobconnect.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/jobseeker")
public class ViewJobSeekerController {

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String redirectToDashboard(@RequestParam String email, HttpSession session) {
        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        // store user details in session
        session.setAttribute("userId", user.get().getId());
        session.setAttribute("userName", user.get().getFullName());
        session.setAttribute("userEmail", user.get().getEmail());
        session.setAttribute("userPhone", user.get().getMobileNumber());

        return "redirect:/jobseeker/dashboard";
    }

    @GetMapping("/dashboard")
    public String jobSeekerHome(HttpSession session,
            Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {

        // Retrieve user details from session
        Long userId = (Long) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");
        String userEmail = (String) session.getAttribute("userEmail");
        String userPhone = (String) session.getAttribute("userPhone");

        if (userId == null) {
            return "redirect:/login"; // redirect to login if session expired
        }

        model.addAttribute("userId", userId);
        model.addAttribute("userName", userName);
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("userPhone", userPhone);

        List<Job> jobs = jobService.searchJobs(keyword, location);
        List<Application> applications = applicationService.getApplicationsByUserId(userId);

        model.addAttribute("jobs", jobs);
        model.addAttribute("applications", applications);
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        model.addAttribute("jobs", jobService.getAllJobs());

        return "jobseeker"; // Thymeleaf template
    }

    @PostMapping("/apply")
    public String applyForJob(
            @RequestParam Long userId,
            @RequestParam Long jobId,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            RedirectAttributes redirectAttributes) {

        Optional<Application> existing = applicationService.findByUserIdAndJobId(userId, jobId);
        if (existing.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You have already applied for this job!");
            return "redirect:/jobseeker/dashboard"; // Redirect so old messages clear
        }

        applicationService.applyForJob(userId, jobId, name, email, phone);

        redirectAttributes.addFlashAttribute("successMessage", "Application submitted successfully!");
        return "redirect:/jobseeker/dashboard"; // Refresh page with new data
    }

    @GetMapping("/search")
    public String searchJobs(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login"; // session expired
        }

        List<Job> jobs = jobService.searchJobs(keyword, location);
        model.addAttribute("jobs", jobs);
        model.addAttribute("userId", userId);
        return "jobseeker";
    }

    @GetMapping("/applied-jobs")
    public String viewAppliedJobs(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        // Fetch all available jobs
        List<Job> jobs = jobService.getAllJobs();

        // Fetch only the jobs this user has applied for
        List<Application> applications = applicationService.getApplicationsByUserId(userId);

        // Add job title + location for each applied job
        List<Map<String, Object>> applicationDetails = new ArrayList<>();

        for (Application app : applications) {
            Map<String, Object> map = new HashMap<>();
            map.put("app", app);

            Job job = jobService.getJobById(app.getJobId());
            if (job != null) {
                map.put("jobTitle", job.getTitle());
                map.put("jobLocation", job.getLocation());
            } else {
                map.put("jobTitle", "Unknown Job");
                map.put("jobLocation", "-");
            }

            applicationDetails.add(map);
        }

        // Send both lists to the same page
        model.addAttribute("jobs", jobs); // All available jobs
        model.addAttribute("applications", applicationDetails); // Applied jobs
        model.addAttribute("userId", userId);

        return "redirect:/jobseeker/dashboard"; // same HTML page
    }

}