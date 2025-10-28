package com.sabpaisa.tokenization.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import com.sabpaisa.tokenization.filter.ApiEncryptionFilter;

/**
 * Configuration for local development profile
 */
@Configuration
@Profile("local")
public class LocalConfig {
    
    /**
     * Disable ApiEncryptionFilter for local development
     */
    @Bean
    public FilterRegistrationBean<ApiEncryptionFilter> disableApiEncryptionFilter(ApiEncryptionFilter filter) {
        FilterRegistrationBean<ApiEncryptionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}