package com.jobportal.jobconnect.servicetests;

import com.jobportal.jobconnect.model.User;
import com.jobportal.jobconnect.service.NotificationService;
import com.jobportal.jobconnect.service.TwilioSMSService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private TwilioSMSService smsService;

    @InjectMocks
    private NotificationService notificationService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setMobileNumber("+919876543210");
    }

    // ✅ Test: sendRegistrationSuccess
    @Test
    void testSendRegistrationSuccess() {
        doNothing().when(smsService).sendSMS(anyString(), anyString());

        notificationService.sendRegistrationSuccess(user);

        verify(smsService, times(1)).sendSMS(
                eq("+919876543210"),
                contains("JobConnect account has been created successfully"));
    }

    // ✅ Test: sendJobAlert
    @Test
    void testSendJobAlert() {
        doNothing().when(smsService).sendSMS(anyString(), anyString());

        notificationService.sendJobAlert(user, "Backend Developer");

        verify(smsService, times(1)).sendSMS(
                eq("+919876543210"),
                contains("Backend Developer"));
    }

    // ✅ Optional sanity check
    @Test
    void testMessageFormatting() {
        // Example of verifying message content manually (optional)
        String expectedMessagePart = "Hi John Doe";
        notificationService.sendJobAlert(user, "Software Engineer");
        verify(smsService).sendSMS(eq("+919876543210"), contains(expectedMessagePart));
    }
}
