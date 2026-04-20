package com.example.user_service.configuration;

import com.example.user_service.filter.InternalAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FilterConfig {
    
    @Bean
    public FilterRegistrationBean<InternalAuthFilter> internalAuthFilterRegistration(
            InternalAuthFilter filter) {
        FilterRegistrationBean<InternalAuthFilter> registration = 
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); 
        return registration;
    }
}