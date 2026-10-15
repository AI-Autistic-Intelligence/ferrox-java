package dev.ferrox.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Global HTTP Filter intercepting all requests to perform real-time Zero-Trust Threat Analysis.
 * Inspects URI paths, query strings, and parameter values against Shannon entropy thresholds
 * and injection signatures (SQLi, XSS, Path Traversal).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SentinelThreatFilter extends OncePerRequestFilter {

    private final SentinelThreatEngine sentinel;

    public SentinelThreatFilter(SentinelThreatEngine sentinel) {
        this.sentinel = sentinel;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        String queryString = request.getQueryString();
        String uri = request.getRequestURI();

        // 1. Inspect URI path
        if (sentinel.evaluateThreat(clientIp, uri)) {
            blockRequest(response, "URI path contains prohibited threat pattern");
            return;
        }

        // 2. Inspect query string
        if (queryString != null && sentinel.evaluateThreat(clientIp, queryString)) {
            blockRequest(response, "Query parameters contain prohibited threat pattern");
            return;
        }

        // 3. Inspect parameter map (handles MockMvc queryParams and form-encoded data)
        if (request.getParameterMap() != null && !request.getParameterMap().isEmpty()) {
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                if (sentinel.evaluateThreat(clientIp, entry.getKey())) {
                    blockRequest(response, "Parameter key contains prohibited threat pattern");
                    return;
                }
                for (String val : entry.getValue()) {
                    if (sentinel.evaluateThreat(clientIp, val)) {
                        blockRequest(response, "Parameter value contains prohibited threat pattern");
                        return;
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void blockRequest(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/problem+json");
        response.getWriter().write(String.format(
                "{\"type\":\"https://ferrox.dev/errors/sentinel-threat-blocked\",\"title\":\"Forbidden\",\"status\":403,\"detail\":\"%s\"}",
                reason
        ));
    }
}
