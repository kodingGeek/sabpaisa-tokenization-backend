package com.sabpaisa.tokenization.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sabpaisa.tokenization.service.AesGcmEncryptionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class ApiEncryptionFilter extends OncePerRequestFilter {
    
    private final AesGcmEncryptionService encryptionService;
    private final ObjectMapper objectMapper;
    
    @Value("${app.encryption.enabled:true}")
    private boolean encryptionEnabled;
    
    @Value("${app.encryption.exclude-paths:/health,/actuator/**,/swagger-ui/**,/v3/api-docs/**}")
    private List<String> excludePaths;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Skip if encryption is disabled
        if (!encryptionEnabled) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Skip for excluded paths
        String requestPath = request.getRequestURI();
        if (isExcludedPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check if client supports encryption via header
        String encryptionHeader = request.getHeader("X-Encryption-Enabled");
        if (!"true".equalsIgnoreCase(encryptionHeader)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Wrap request and response for encryption/decryption
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        try {
            // Process the request
            processRequest(wrappedRequest);
            
            // Continue with filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
            // Process the response
            processResponse(wrappedRequest, wrappedResponse);
            
        } catch (Exception e) {
            log.error("Error in encryption filter: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Encryption/Decryption failed\"}");
        }
    }
    
    private void processRequest(ContentCachingRequestWrapper request) throws IOException {
        if (!"POST".equals(request.getMethod()) && !"PUT".equals(request.getMethod())) {
            return;
        }
        
        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return;
        }
        
        // Read encrypted request body
        byte[] requestBody = StreamUtils.copyToByteArray(request.getInputStream());
        if (requestBody.length == 0) {
            return;
        }
        
        try {
            // Parse encrypted request
            EncryptedRequest encryptedRequest = objectMapper.readValue(requestBody, EncryptedRequest.class);
            
            // Generate AAD from request metadata
            String aad = generateAAD(request);
            
            // Decrypt the payload
            String decryptedPayload = encryptionService.decrypt(encryptedRequest.getEncryptedData(), aad);
            
            // Set decrypted content as request attribute for later use
            request.setAttribute("decrypted_content", decryptedPayload);
            
            log.debug("Request decrypted successfully for path: {}", request.getRequestURI());
            
        } catch (Exception e) {
            log.error("Failed to decrypt request: ", e);
            throw new RuntimeException("Request decryption failed", e);
        }
    }
    
    private void processResponse(HttpServletRequest request, ContentCachingResponseWrapper response) 
            throws IOException {
        
        // Get response content
        byte[] responseBody = response.getContentAsByteArray();
        if (responseBody.length == 0) {
            response.copyBodyToResponse();
            return;
        }
        
        // Only encrypt JSON responses
        String contentType = response.getContentType();
        if (contentType == null || !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            response.copyBodyToResponse();
            return;
        }
        
        try {
            String originalResponse = new String(responseBody, StandardCharsets.UTF_8);
            
            // Generate AAD from request metadata
            String aad = generateAAD(request);
            
            // Encrypt the response
            String encryptedData = encryptionService.encrypt(originalResponse, aad);
            
            // Create encrypted response
            EncryptedResponse encryptedResponse = new EncryptedResponse();
            encryptedResponse.setEncryptedData(encryptedData);
            encryptedResponse.setTimestamp(System.currentTimeMillis());
            encryptedResponse.setAlgorithm("AES-256-GCM");
            
            // Write encrypted response
            byte[] encryptedResponseBytes = objectMapper.writeValueAsBytes(encryptedResponse);
            response.resetBuffer();
            response.setContentLength(encryptedResponseBytes.length);
            response.getOutputStream().write(encryptedResponseBytes);
            response.copyBodyToResponse();
            
            log.debug("Response encrypted successfully for path: {}", request.getRequestURI());
            
        } catch (Exception e) {
            log.error("Failed to encrypt response: ", e);
            response.copyBodyToResponse();
        }
    }
    
    private String generateAAD(HttpServletRequest request) {
        // Generate Additional Authenticated Data from request metadata
        return String.format("%s:%s:%s:%s",
            request.getMethod(),
            request.getRequestURI(),
            request.getHeader("X-Request-ID") != null ? request.getHeader("X-Request-ID") : "",
            request.getSession().getId()
        );
    }
    
    private boolean isExcludedPath(String path) {
        return excludePaths.stream().anyMatch(excludePath -> {
            if (excludePath.endsWith("/**")) {
                String prefix = excludePath.substring(0, excludePath.length() - 3);
                return path.startsWith(prefix);
            }
            return path.equals(excludePath);
        });
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Skip for non-API paths
        String path = request.getRequestURI();
        return !path.startsWith("/api/");
    }
    
    // DTO classes for encrypted requests/responses
    public static class EncryptedRequest {
        private String encryptedData;
        private String algorithm;
        private Long timestamp;
        
        // Getters and setters
        public String getEncryptedData() { return encryptedData; }
        public void setEncryptedData(String encryptedData) { this.encryptedData = encryptedData; }
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
    
    public static class EncryptedResponse {
        private String encryptedData;
        private String algorithm;
        private Long timestamp;
        
        // Getters and setters
        public String getEncryptedData() { return encryptedData; }
        public void setEncryptedData(String encryptedData) { this.encryptedData = encryptedData; }
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
}