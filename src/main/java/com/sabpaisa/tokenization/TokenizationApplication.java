package com.sabpaisa.tokenization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class TokenizationApplication extends SpringBootServletInitializer {
    
    public static void main(String[] args) {
        SpringApplication.run(TokenizationApplication.class, args);
    }
}