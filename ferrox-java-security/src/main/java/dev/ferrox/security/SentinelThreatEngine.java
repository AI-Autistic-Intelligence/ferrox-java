package dev.ferrox.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * High-performance Zero-Trust Threat Detection Engine.
 * Evaluates Shannon Entropy in O(N) linear time and analyzes inputs against known injection vectors.
 */
@Service
public class SentinelThreatEngine {

    private static final Logger log = LoggerFactory.getLogger(SentinelThreatEngine.class);

    private static final Pattern SQLI_PATTERN = Pattern.compile(
            "(?i)(union\\s+select|select\\s+.*\\s+from|insert\\s+into|drop\\s+table|delete\\s+from|--|;\\s*select|or\\s+1=1)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script[^>]*>.*?</script>|<script|javascript:|onerror\\s*=|onload\\s*=|alert\\s*\\()",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
            "(\\.\\./|\\.\\.\\\\|/etc/passwd|windows/system32)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Evaluates whether a client IP and associated request payload constitute a security threat.
     *
     * @param clientIp Client IP address
     * @param payload Request body, path, or query string
     * @return true if threat is detected, false otherwise
     */
    public boolean evaluateThreat(String clientIp, String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return false;
        }

        // 1. Signature checks for direct exploit attempts
        if (SQLI_PATTERN.matcher(payload).find()) {
            log.warn("[Sentinel] SQLi signature intercepted from IP {}: {}", clientIp, payload);
            return true;
        }

        if (XSS_PATTERN.matcher(payload).find()) {
            log.warn("[Sentinel] XSS signature intercepted from IP {}: {}", clientIp, payload);
            return true;
        }

        if (PATH_TRAVERSAL_PATTERN.matcher(payload).find()) {
            log.warn("[Sentinel] Path traversal signature intercepted from IP {}: {}", clientIp, payload);
            return true;
        }

        // 2. Mathematical Shannon Entropy check for obfuscated or encrypted shellcode
        if (payload.length() > 64) {
            double entropy = calculateShannonEntropy(payload);
            if (entropy > 4.8) {
                log.warn("[Sentinel] High Shannon entropy ({}) detected from IP {}: {}", entropy, clientIp, payload);
                return true;
            }
        }

        return false;
    }

    public double calculateShannonEntropy(String s) {
        if (s == null || s.isEmpty()) return 0.0;

        int[] frequencies = new int[256];
        for (char c : s.toCharArray()) {
            if (c < 256) {
                frequencies[c]++;
            }
        }

        double entropy = 0.0;
        int length = s.length();
        for (int count : frequencies) {
            if (count > 0) {
                double p = (double) count / length;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }
        return entropy;
    }
}
