package com.sabpaisa.tokenization.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {
    
    @Value("${app.sms.provider:mock}")
    private String smsProvider;
    
    @Value("${app.sms.api-key}")
    private String apiKey;
    
    @Value("${app.sms.sender-id:SBPAIS}")
    private String senderId;
    
    public void sendSms(String phoneNumber, String message) {
        try {
            // In production, integrate with actual SMS provider (Twilio, AWS SNS, etc.)
            if ("mock".equals(smsProvider)) {
                log.info("SMS Mock - To: {}, Message: {}", phoneNumber, message);
                return;
            }
            
            // Example integration with SMS provider
            switch (smsProvider.toLowerCase()) {
                case "twilio":
                    sendViaTwilio(phoneNumber, message);
                    break;
                case "aws-sns":
                    sendViaAwsSns(phoneNumber, message);
                    break;
                case "textlocal":
                    sendViaTextLocal(phoneNumber, message);
                    break;
                default:
                    log.warn("Unknown SMS provider: {}", smsProvider);
            }
            
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: ", phoneNumber, e);
        }
    }
    
    private void sendViaTwilio(String phoneNumber, String message) {
        // Twilio implementation
        log.info("Sending SMS via Twilio to: {}", phoneNumber);
    }
    
    private void sendViaAwsSns(String phoneNumber, String message) {
        // AWS SNS implementation
        log.info("Sending SMS via AWS SNS to: {}", phoneNumber);
    }
    
    private void sendViaTextLocal(String phoneNumber, String message) {
        // TextLocal implementation
        log.info("Sending SMS via TextLocal to: {}", phoneNumber);
    }
}