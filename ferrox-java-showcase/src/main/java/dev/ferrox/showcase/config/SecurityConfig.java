package dev.ferrox.showcase.config;

import dev.ferrox.security.SentinelThreatFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Enterprise Stateless Security Configuration for Ferrox Java Showcase.
 * Configures stateless session policy, disables CSRF for REST APIs,
 * wires Sentinel Threat Filter into the security filter chain,
 * and delegates granular token verification to PASETO and Sentinel Threat Engine.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SentinelThreatFilter sentinelThreatFilter;

    public SecurityConfig(SentinelThreatFilter sentinelThreatFilter) {
        this.sentinelThreatFilter = sentinelThreatFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(sentinelThreatFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api/v1/auth/**",
                    "/api/v1/users/**",
                    "/api/v1/auctions/**",
                    "/api/v1/market/**",
                    "/api/v1/resilience/**",
                    "/api/v1/secure/**"
                ).permitAll()
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
