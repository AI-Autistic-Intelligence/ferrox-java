package dev.ferrox.web;

import dev.ferrox.core.AppError;
import dev.ferrox.core.ErrorCode;
import dev.ferrox.security.SentinelThreatEngine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Layer 1-3: Security, MTD, Sentinel Threat Engine.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SentinelFilter extends OncePerRequestFilter {

    private final SentinelThreatEngine threatEngine;

    public SentinelFilter(SentinelThreatEngine threatEngine) {
        this.threatEngine = threatEngine;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Strip headers
        response.setHeader("Server", "Ferrox-Sentinel");

        // Threat Engine evaluation
        String clientIp = request.getRemoteAddr();
        if (threatEngine.evaluateThreat(clientIp, null)) {
            response.setStatus(429); // Or 403 based on threat
            response.getWriter().write("{\"error\": \"Threat detected by Sentinel\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
