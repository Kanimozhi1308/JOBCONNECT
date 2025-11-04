package com.jobportal.jobconnect.controllertests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jobportal.jobconnect.controller.ViewController;
import com.jobportal.jobconnect.dto.UserLoginDTO;
import com.jobportal.jobconnect.dto.UserSignupDTO;
import com.jobportal.jobconnect.dto.UserResponseDTO;
import com.jobportal.jobconnect.model.Role;
import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.service.NotificationService;
import com.jobportal.jobconnect.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ViewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private NotificationService notificationService;

    private UserSignupDTO signupDTO;
    private UserLoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        signupDTO = new UserSignupDTO();
        signupDTO.setName("Kani");
        signupDTO.setEmail("kani@example.com");
        signupDTO.setPassword("test123");
        signupDTO.setPhone("9876543210");
        signupDTO.setRole(Role.JOB_SEEKER);

        loginDTO = new UserLoginDTO();
        loginDTO.setEmail("kani@example.com");
        loginDTO.setPassword("test123");
    }

    // ==================== TESTS ====================

    @Test
    void testLandingPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("signupDTO", "loginDTO"));
    }

    @Test
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginDTO"));
    }

    @Test
    void testSignupPage() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("signupDTO"));
    }

    @Test
    void testSignupSuccess() throws Exception {
        // Given a mock UserSignupDTO
        UserSignupDTO signupDTO = new UserSignupDTO();
        signupDTO.setName("Kani");
        signupDTO.setEmail("kani@example.com");
        signupDTO.setPassword("password");
        signupDTO.setPhone("9876543210");
        signupDTO.setRole(Role.JOB_SEEKER);

        // Mock the UserResponseDTO that should be returned by the service
        UserResponseDTO mockResponse = new UserResponseDTO();
        mockResponse.setId(1L);
        mockResponse.setName("Kani");
        mockResponse.setEmail("kani@example.com");
        mockResponse.setRole(Role.JOB_SEEKER);

        // ✅ Correct mocking for method that returns UserResponseDTO
        when(userService.registerUser(any(UserSignupDTO.class))).thenReturn(mockResponse);

        // Notification service is a void method → doNothing()
        doNothing().when(notificationService).sendRegistrationSuccess(any(User.class));

        // When signup is performed
        mockMvc.perform(post("/signup")
                .flashAttr("signupDTO", signupDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Verify both services were called
        verify(userService, times(1)).registerUser(any(UserSignupDTO.class));
    }

    @Test
    void testSignupFailure() throws Exception {
        doThrow(new RuntimeException("Email already exists"))
                .when(userService).registerUser(any(UserSignupDTO.class));

        mockMvc.perform(post("/signup")
                .flashAttr("signupDTO", signupDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup"));
    }

    @Test
    void testLoginSuccessForEmployer() throws Exception {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setRole(Role.EMPLOYER);

        when(userService.loginUser(any(UserLoginDTO.class))).thenReturn("mockToken");
        when(userService.getUserByEmail(anyString())).thenReturn(response);

        mockMvc.perform(post("/login")
                .flashAttr("loginDTO", loginDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employer/dashboard"));
    }

    @Test
    void testLoginSuccessForJobSeeker() throws Exception {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(2L);
        response.setRole(Role.JOB_SEEKER);

        when(userService.loginUser(any(UserLoginDTO.class))).thenReturn("mockToken");
        when(userService.getUserByEmail(anyString())).thenReturn(response);

        mockMvc.perform(post("/login")
                .flashAttr("loginDTO", loginDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jobseeker/dashboard"));
    }

    @Test
    void testLoginFailure() throws Exception {
        when(userService.loginUser(any(UserLoginDTO.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/login")
                .flashAttr("loginDTO", loginDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginError"));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
