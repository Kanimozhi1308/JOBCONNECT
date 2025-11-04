package com.jobportal.jobconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.jobconnect.dto.UserLoginDTO;
import com.jobportal.jobconnect.dto.UserResponseDTO;
import com.jobportal.jobconnect.dto.UserSignupDTO;
import com.jobportal.jobconnect.model.Role;
import com.jobportal.jobconnect.service.NotificationService;
import com.jobportal.jobconnect.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    // ===== Landing Page with both Signup and Login =====
    @GetMapping("/")
    public String landingPage(Model model) {
        model.addAttribute("signupDTO", new UserSignupDTO());
        model.addAttribute("loginDTO", new UserLoginDTO());
        return "index"; // templates/index.html
    }

    // ===== Login Page =====
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginDTO", new UserLoginDTO());
        return "login"; // templates/login.html
    }

    // ===== Signup Page =====
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupDTO", new UserSignupDTO());
        return "signup"; // templates/signup.html
    }

    // ===== Signup Handler =====
    @PostMapping("/signup")
    public String signup(@ModelAttribute("signupDTO") UserSignupDTO signupDTO,
            RedirectAttributes redirectAttributes) {

        if (signupDTO.getName() == null || signupDTO.getEmail() == null ||
                signupDTO.getPassword() == null || signupDTO.getPhone() == null ||
                signupDTO.getRole() == null) {

            redirectAttributes.addFlashAttribute("signupError", "All fields are mandatory!");
            return "redirect:/signup";
        }

        if (!signupDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            redirectAttributes.addFlashAttribute("signupError", "Invalid email format!");
            return "redirect:/signup";
        }
        try {
            userService.registerUser(signupDTO);
            redirectAttributes.addFlashAttribute("signupMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("signupError", e.getMessage());
            return "redirect:/signup";
        }
    }

    // ===== Login Handler =====
    @PostMapping("/login")
    public String login(@ModelAttribute("loginDTO") UserLoginDTO loginDTO,
            Model model,
            HttpSession session) {
        try {

            // 🔒 Step 1: Validate credentials using service
            String message = userService.loginUser(loginDTO);

            // Validate login
            UserResponseDTO userDto = userService.getUserByEmail(loginDTO.getEmail());

            // Store user info in session
            session.setAttribute("userId", userDto.getId());
            session.setAttribute("role", userDto.getRole());
            session.setAttribute("userName", userDto.getName());

            // Redirect based on role
            if (userDto.getRole() == Role.EMPLOYER) {
                return "redirect:/employer/dashboard"; // handled by ViewEmployerController
            } else if (userDto.getRole() == Role.JOB_SEEKER) {
                return "redirect:/jobseeker/dashboard"; // handled by JobSeekerController
            } else {
                model.addAttribute("loginError", "Invalid user role.");
                return "login";
            }
        } catch (RuntimeException e) {
            model.addAttribute("loginError", e.getMessage());
            return "login";
        }
    }

    // ===== Logout Handler =====
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // clear session
        return "redirect:/login";
    }

}