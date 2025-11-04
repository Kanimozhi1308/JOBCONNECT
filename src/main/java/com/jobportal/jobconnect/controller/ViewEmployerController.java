package com.jobportal.jobconnect.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.jobportal.jobconnect.model.Application;
import com.jobportal.jobconnect.model.Job;
import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.service.ApplicationService;
import com.jobportal.jobconnect.service.EmployerJobService;
import com.jobportal.jobconnect.service.JobService;
import com.jobportal.jobconnect.service.NotificationService;

@Controller
@RequestMapping("/employer")
public class ViewEmployerController {

    @Autowired
    private EmployerJobService employerJobService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JobService jobService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long employerId = (Long) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");
        if (employerId == null)
            return "redirect:/login";

        model.addAttribute("employerId", employerId);
        model.addAttribute("userName", userName);
        model.addAttribute("jobs", employerJobService.getByEmployer(employerId));

        // Fetch applications with applicant details + job title
        List<Application> apps = applicationService.getApplicationsByEmployer(employerId);
        List<Map<String, Object>> appDetails = new ArrayList<>();

        for (Application app : apps) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("status", app.getStatus());

            // Fetch job info
            Job job = jobService.getJobById(app.getJobId());
            map.put("jobTitle", job != null ? job.getTitle() : "Unknown Job");

            // Fetch applicant info
            User applicant = applicationService.getApplicantById(app.getUserId());
            if (applicant != null) {
                map.put("applicantName", app.getApplicantName());
                map.put("applicantEmail", app.getApplicantEmail());
                map.put("applicantPhone", app.getApplicantPhone());
            }

            appDetails.add(map);
        }

        model.addAttribute("applications", appDetails);

        return "employer";
    }

    // Add Job
    @PostMapping("/save")
    public String saveJob(@ModelAttribute Job job, HttpSession session) {
        Long employerId = (Long) session.getAttribute("userId");

        employerJobService.saveAllJobs(Collections.singletonList(job));

        return "redirect:/employer/dashboard";
    }

    // Update Job
    @PostMapping("/update")
    public String updateJob(@RequestParam Long jobId,
            @ModelAttribute Job updatedJob,
            HttpSession session) {
        Long employerId = (Long) session.getAttribute("userId");
        employerJobService.updateJob(jobId, updatedJob, employerId);
        return "redirect:/employer/dashboard";
    }

    // ================= DELETE JOB =================
    @GetMapping("/delete/{id}")
    public String deleteJob(@PathVariable Long id) {
        jobService.deleteJobById(id);
        return "redirect:/employer/dashboard"; // redirect to job list page
    }

    // ✅ Display job list page
    @GetMapping("/list")
    public String listJobs(Model model) {
        model.addAttribute("jobs", jobService.getAllJobs());
        return "employer"; // your HTML file name (job-list.html)
    }

    // Update Application Status
    @PostMapping("/application-status")
    public String updateApplicationStatus(
            @RequestParam Long applicationId,
            @RequestParam String status,
            HttpSession session) {

        Long employerId = (Long) session.getAttribute("userId");
        Application updatedApp = applicationService.updateStatus(applicationId, status, employerId);

        return "redirect:/employer/dashboard";
    }
}
