package edu.rpi.giveaway.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WebAuthConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<GoogleAuthFilter> googleAuthFilterRegistration(
            GoogleTokenVerifierService tokenVerifierService,
            ObjectMapper objectMapper
    ) {
        FilterRegistrationBean<GoogleAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new GoogleAuthFilter(tokenVerifierService, objectMapper));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
