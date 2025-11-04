package com.jobportal.jobconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jobportal.jobconnect.model.User;

@Service
public class NotificationService {

    @Autowired
    private TwilioSMSService smsService;

    /** 📩 Sends SMS after successful registration. */
    public void sendRegistrationSuccess(User user) {
        String message = "Hi     " + user.getFullName()
                + "!🎉Your JobConnect account has been created successfully.";
        smsService.sendSMS(user.getMobileNumber(), message);
    }

    /** 📩 Sends job alert to job seekers when new jobs are posted. */
    public void sendJobAlert(User user, String title) {
        String message = "Hi " + user.getFullName() +
                "! ✨ The universe just opened a new door — '" + title +
                "'. Take the leap and make it yours on JobConnect!";
        smsService.sendSMS(user.getMobileNumber(), message);
    }

}
