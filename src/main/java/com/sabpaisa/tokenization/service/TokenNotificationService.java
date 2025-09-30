package com.sabpaisa.tokenization.service;

import com.sabpaisa.tokenization.domain.entity.EnhancedToken;
import com.sabpaisa.tokenization.repository.EnhancedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TokenNotificationService {
    
    private final EnhancedTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final SmsService smsService;
    
    @Value("${app.notification.from-email}")
    private String fromEmail;
    
    @Value("${app.notification.enabled:true}")
    private boolean notificationEnabled;
    
    @Scheduled(cron = "0 0 9 * * *") // Run daily at 9 AM
    public void checkAndSendExpiryNotifications() {
        if (!notificationEnabled) {
            return;
        }
        
        log.info("Starting token expiry notification check");
        
        LocalDateTime now = LocalDateTime.now();
        List<EnhancedToken> tokensNearExpiry = tokenRepository.findTokensNearExpiry(now);
        
        int emailsSent = 0;
        int smsSent = 0;
        
        for (EnhancedToken token : tokensNearExpiry) {
            try {
                if (shouldSendNotification(token, now)) {
                    if (token.getCustomerEmail() != null) {
                        sendEmailNotification(token);
                        emailsSent++;
                    }
                    
                    if (token.getCustomerPhone() != null) {
                        sendSmsNotification(token);
                        smsSent++;
                    }
                    
                    token.setLastNotificationSent(now);
                    tokenRepository.save(token);
                }
            } catch (Exception e) {
                log.error("Error sending notification for token {}: {}", token.getTokenValue(), e.getMessage());
            }
        }
        
        log.info("Token expiry notification check completed. Emails sent: {}, SMS sent: {}", emailsSent, smsSent);
    }
    
    private boolean shouldSendNotification(EnhancedToken token, LocalDateTime now) {
        if (!token.getNotificationEnabled() || !token.getIsActive()) {
            return false;
        }
        
        // Check if we've already sent a notification recently
        if (token.getLastNotificationSent() != null) {
            LocalDateTime lastSent = token.getLastNotificationSent();
            if (lastSent.plusDays(7).isAfter(now)) { // Don't send more than once a week
                return false;
            }
        }
        
        // Check if token is within notification window
        LocalDateTime expiryThreshold = now.plusDays(token.getDaysBeforeExpiryNotification());
        return token.getExpiryDate().isBefore(expiryThreshold) && token.getExpiryDate().isAfter(now);
    }
    
    private void sendEmailNotification(EnhancedToken token) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(token.getCustomerEmail());
        helper.setSubject("Token Expiry Notification - Action Required");
        
        String htmlContent = buildEmailContent(token);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
        log.info("Email notification sent to {} for token ending in {}", 
                token.getCustomerEmail(), token.getCardLast4());
    }
    
    private void sendSmsNotification(EnhancedToken token) {
        String message = String.format(
            "Your payment token ending in %s will expire on %s. Please renew it to continue using services on %s. - SabPaisa",
            token.getCardLast4(),
            token.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
            token.getPlatform() != null ? token.getPlatform().getPlatformName() : "our platform"
        );
        
        smsService.sendSms(token.getCustomerPhone(), message);
        log.info("SMS notification sent to {} for token ending in {}", 
                token.getCustomerPhone(), token.getCardLast4());
    }
    
    private String buildEmailContent(EnhancedToken token) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #1976d2; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f5f5f5; }
                    .button { background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; display: inline-block; margin-top: 10px; }
                    .footer { text-align: center; padding: 20px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>Token Expiry Notification</h2>
                    </div>
                    <div class="content">
                        <p>Dear Customer,</p>
                        <p>Your payment token details:</p>
                        <ul>
                            <li>Card ending in: %s</li>
                            <li>Platform: %s</li>
                            <li>Expiry Date: %s</li>
                            <li>Days until expiry: %d</li>
                        </ul>
                        <p>Please take action to renew your token to ensure uninterrupted service.</p>
                        <a href="#" class="button">Renew Token</a>
                    </div>
                    <div class="footer">
                        <p>This is an automated notification from SabPaisa Tokenization Platform</p>
                        <p>Please do not reply to this email</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            token.getCardLast4(),
            token.getPlatform() != null ? token.getPlatform().getPlatformName() : "All Platforms",
            token.getExpiryDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
            java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), token.getExpiryDate())
        );
    }
}