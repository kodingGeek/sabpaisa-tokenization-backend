package com.sabpaisa.tokenization.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${app.notification.email.from:noreply@sabpaisa.com}")
    private String fromEmail;
    
    @Value("${app.notification.email.security-team:security@sabpaisa.com}")
    private String securityTeamEmail;
    
    @Value("${app.notification.email.enabled:false}")
    private boolean emailEnabled;
    
    // In-memory notification store for demo purposes
    private final List<SecurityAlert> alertHistory = new ArrayList<>();
    
    public void sendSecurityAlert(String subject, String message, String severity) {
        logger.info("Security Alert [{}]: {} - {}", severity, subject, message);
        
        // Store alert in history
        SecurityAlert alert = new SecurityAlert();
        alert.setSubject(subject);
        alert.setMessage(message);
        alert.setSeverity(severity);
        alert.setTimestamp(LocalDateTime.now());
        alertHistory.add(alert);
        
        // Send email notification if enabled
        if (emailEnabled && mailSender != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    SimpleMailMessage mailMessage = new SimpleMailMessage();
                    mailMessage.setFrom(fromEmail);
                    mailMessage.setTo(securityTeamEmail);
                    mailMessage.setSubject("[" + severity + "] " + subject);
                    mailMessage.setText(message + "\n\nTimestamp: " + LocalDateTime.now());
                    
                    mailSender.send(mailMessage);
                    logger.info("Security alert email sent successfully");
                } catch (Exception e) {
                    logger.error("Failed to send security alert email", e);
                }
            });
        }
        
        // In production, you would also:
        // 1. Send to a real-time dashboard via WebSocket
        // 2. Send SMS for CRITICAL alerts
        // 3. Send to Slack/Teams webhook
        // 4. Create ticket in incident management system
    }
    
    public void sendMerchantNotification(String merchantId, String subject, String message) {
        logger.info("Merchant Notification [{}]: {} - {}", merchantId, subject, message);
        
        // In production, this would:
        // 1. Look up merchant's notification preferences
        // 2. Send via configured channels (email, SMS, webhook)
        // 3. Store notification in database
    }
    
    public List<SecurityAlert> getRecentAlerts(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return alertHistory.stream()
            .filter(alert -> alert.getTimestamp().isAfter(since))
            .toList();
    }
    
    public static class SecurityAlert {
        private String subject;
        private String message;
        private String severity;
        private LocalDateTime timestamp;
        
        // Getters and setters
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}