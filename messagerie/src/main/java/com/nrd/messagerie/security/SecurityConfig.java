package com.nrd.messagerie.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**", "/actuator/**", "/api/messages/debug-jwt", "/api/messages/test", "/api/messages/health").permitAll()
                        .requestMatchers(
                            "/api/messages/send",
                            "/api/messages/conversations", 
                            "/api/messages/conversation-partners",
                            "/api/messages/history",
                            "/api/messages/unread-count",
                            "/api/messages/conversation/*",
                            "/api/messages/history/*",
                            "/api/messages/block/*",
                            "/api/messages/websocket-status",
                            "/api/messages/debug-sessions",
                            "/api/messages/test-websocket/*",
                            "/api/messages/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}