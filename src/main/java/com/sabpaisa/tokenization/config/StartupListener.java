package com.sabpaisa.tokenization.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener that logs when the application is fully started and ready
 * This helps with debugging ECS health check issues
 */
@Component
@Slf4j
public class StartupListener implements ApplicationListener<ApplicationReadyEvent> {
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("=====================================================");
        log.info("APPLICATION FULLY STARTED AND READY TO ACCEPT TRAFFIC");
        log.info("Health check endpoint: /api/actuator/health");
        log.info("Swagger UI: /api/swagger-ui.html");
        log.info("API Docs: /api/v3/api-docs");
        log.info("=====================================================");
    }
}