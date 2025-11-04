package com.jobportal.jobconnect.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class TwilioSMSService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.messaging.service.sid}")
    private String messagingServiceSid;

    @Value("${twilio.sender.name}")
    private String senderName;

    /** Sends SMS using Twilio Messaging Service. */
    public void sendSMS(String to, String messageBody) {

        try {
            Twilio.init(accountSid, authToken);

            // Ensure the number starts with +91
            if (!to.startsWith("+")) {
                to = "+91" + to;
            }

            // Include sender name in the message content
            String finalMessage = "📢 " + senderName + ": " + messageBody;

            Message message = Message.creator(
                    new PhoneNumber(to),
                    messagingServiceSid, // fromNumber if not using messaging service
                    finalMessage).create();

            System.out.println("✅ SMS sent successfully: " + message.getSid());

        } catch (Exception e) {
            System.out.println("⚠️ SMS sending failed: " + e.getMessage());

        }
    }
}