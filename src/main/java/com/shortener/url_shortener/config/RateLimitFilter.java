package com.shortener.url_shortener.config;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.shortener.url_shortener.service.RateLimitService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();

        boolean allowed;

        if (path.equals("/api/shorten") && request.getMethod().equals("POST")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean authenticated = auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal());

            String key = "rate:shorten:" + ip;
            int limit = authenticated ? 1000 : 100;
            allowed = rateLimitService.isAllowed(key, limit, Duration.ofHours(1));

        } else if (!path.startsWith("/api/") && !path.startsWith("/swagger")
                && !path.startsWith("/v3/api-docs")) {
            String key = "rate:redirect:" + ip;
            allowed = rateLimitService.isAllowed(key, 200, Duration.ofMinutes(1));

        } else {
            filterChain.doFilter(request, response);
            return;
        }

        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Too many requests. Try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}