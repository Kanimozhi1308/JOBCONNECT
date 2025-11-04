package com.jobportal.jobconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.jobconnect.service.TwilioSMSService;

@RestController
@RequestMapping("/api/notifications")
public class TwilioSMSController {

    @Autowired
    private TwilioSMSService twilioService;

    @PostMapping("/send")
    public String sendTestSMS(@RequestParam String to, @RequestParam String message) {
        twilioService.sendSMS(to, message);
        return "SMS sent (check logs for errors)";
    }
}