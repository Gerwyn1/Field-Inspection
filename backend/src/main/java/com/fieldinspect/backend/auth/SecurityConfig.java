package com.fieldinspect.backend.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * The security rulebook. In Express terms this whole class is the part of
 * server.js where you decide the middleware order and which routers get
 * `requireAuth` and which don't.
 *
 * @Configuration -> "this class declares beans" (objects Spring should build
 *                   and manage — like exports a DI container picks up).
 * @Bean          -> each method produces one such object.
 *
 * Just ADDING spring-boot-starter-security locks every endpoint behind a login
 * form with a random password — secure-by-default. Defining our own
 * SecurityFilterChain bean REPLACES that default with our rules.
 */
@Configuration
@EnableMethodSecurity // turns on @PreAuthorize checks on individual methods
public class SecurityConfig {

    /** BCrypt hasher (like bcryptjs). encode() to hash, matches() to check. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                // CSRF protection defends session-cookie auth; we use stateless
                // Bearer tokens, so it's unnecessary (and would block our POSTs).
                .csrf(csrf -> csrf.disable())

                // No server-side sessions — every request must carry its own token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // The route rules, checked top to bottom, first match wins.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/ping").permitAll() // login + health stay public
                        .requestMatchers("/h2-console/**").permitAll()           // dev DB console
                        // Spring renders 4xx/5xx bodies via an internal forward to
                        // "/error" — if that stays protected, every error from a
                        // public endpoint (e.g. a 400 validation failure on login)
                        // gets swallowed and resurfaces as a misleading 401.
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())                           // EVERYTHING else needs a token

                // When an unauthenticated request hits a protected route, answer
                // plain 401 (default would be 403 — wrong semantics: 401 = "who are
                // you?", 403 = "I know you, and no").
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // The H2 console renders itself in an <iframe>; allow same-origin framing.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // Run OUR filter before Spring's own username/password one.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}