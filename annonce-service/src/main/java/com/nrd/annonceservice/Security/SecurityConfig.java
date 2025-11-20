package com.nrd.annonceservice.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactive CSRF (API REST → stateless)
                .csrf(csrf -> csrf.disable())

                // Autorise l'utilisation de frames (nécessaire pour H2 Console)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // Pas de session côté serveur → JWT seulement
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Règles d'accès
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics
                        .requestMatchers("/api/annonces/public/**").permitAll()
                        .requestMatchers("/api/annonces/villes").permitAll()
                        .requestMatchers("/api/annonces/search").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // Endpoints admin
                        .requestMatchers("/api/annonces/admin/**").hasRole("ADMIN")
                        // Endpoints utilisateur
                        .requestMatchers("/api/annonces/my-annonces").hasRole("USER")
                        .requestMatchers("/api/annonces/user/**").hasRole("USER")
                        .requestMatchers("/api/annonces/notifications/**").hasRole("USER")
                        .requestMatchers("/api/annonces/*/signaler").hasRole("USER")
                        // Détails d'annonce - PUBLIC
                        .requestMatchers("/api/annonces/*").permitAll()
                        .anyRequest().authenticated()
                )

                // Ajout du filtre JWT
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
