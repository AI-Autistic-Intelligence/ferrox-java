package dev.ferrox.security;

import org.springframework.stereotype.Service;

@Service
public class SentinelThreatEngine {

    // Simulates checking the request velocity and calculating Shannon Entropy for payloads.
    // Based on ferrox (Rust) SOTA literature innovation #6 and #3.
    public boolean evaluateThreat(String clientIp, String payload) {
        if (payload != null && !payload.isEmpty()) {
            double entropy = calculateShannonEntropy(payload);
            // High entropy often indicates encrypted/obfuscated malicious payloads or shellcode
            if (entropy > 4.5) { 
                return true;
            }
        }
        
        // Z-Score Velocity check (Pseudo)
        double zScore = calculateVelocityZScore(clientIp);
        if (zScore > 3.0) {
            return true;
        }
        
        return false;
    }
    
    private double calculateShannonEntropy(String s) {
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
    
    private double calculateVelocityZScore(String clientIp) {
        // In reality, this would query a Redis sliding window
        return 0.5;
    }
}
