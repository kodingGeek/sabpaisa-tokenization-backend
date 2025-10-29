package com.sabpaisa.tokenization.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Listener that logs when the application is fully started and ready
 * This helps with debugging ECS health check issues
 */
@Component
@Slf4j
public class StartupListener implements ApplicationListener<ApplicationReadyEvent> {
    
    @Autowired
    private Environment environment;
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    @Value("${server.port:8082}")
    private String serverPort;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String baseUrl = "http://localhost:" + serverPort + contextPath;
        
        log.info("=====================================================");
        log.info("APPLICATION FULLY STARTED AND READY TO ACCEPT TRAFFIC");
        log.info("Active profiles: {}", String.join(",", environment.getActiveProfiles()));
        log.info("Server port: {}", serverPort);
        log.info("Context path: {}", contextPath.isEmpty() ? "/" : contextPath);
        log.info("Health check endpoint: {}/actuator/health", baseUrl);
        log.info("Readiness endpoint: {}/actuator/health/readiness", baseUrl);
        log.info("Liveness endpoint: {}/actuator/health/liveness", baseUrl);
        log.info("Swagger UI: {}/swagger-ui.html", baseUrl);
        log.info("API Docs: {}/v3/api-docs", baseUrl);
        
        // Test database connectivity
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                log.info("Database connection: SUCCESSFUL");
            } catch (Exception e) {
                log.error("Database connection: FAILED - {}", e.getMessage());
            }
        } else {
            log.warn("Database: NOT CONFIGURED");
        }
        
        log.info("=====================================================");
    }
}