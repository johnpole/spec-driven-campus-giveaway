package edu.rpi.giveaway.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class GoogleAuthFilter extends OncePerRequestFilter {

    private final GoogleTokenVerifierService tokenVerifierService;
    private final ObjectMapper objectMapper;

    public GoogleAuthFilter(GoogleTokenVerifierService tokenVerifierService, ObjectMapper objectMapper) {
        this.tokenVerifierService = tokenVerifierService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                UserPrincipal user = tokenVerifierService.verifyToken(token);
                UserContext.setCurrentUser(user);
            } catch (UnauthorizedException ex) {
                UserContext.clear();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(), Map.of(
                        "error", "Unauthorized",
                        "message", ex.getMessage()
                ));
                return;
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
