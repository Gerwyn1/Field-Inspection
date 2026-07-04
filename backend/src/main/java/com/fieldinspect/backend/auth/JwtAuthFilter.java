package com.fieldinspect.backend.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fieldinspect.backend.user.AppUserRepository;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Our custom middleware. The Express equivalent, almost line for line:
 *
 *   function jwtAuth(req, res, next) {
 *     const header = req.headers.authorization;
 *     if (header?.startsWith('Bearer ')) {
 *       try {
 *         const email = jwt.verify(header.slice(7), SECRET).sub;
 *         req.user = await users.findByEmail(email);   // <- SecurityContextHolder
 *       } catch {} // invalid token -> just stay logged-out
 *     }
 *     next();                                          // <- filterChain.doFilter
 *   }
 *
 * OncePerRequestFilter = a base class guaranteeing we run exactly once per request.
 * SecurityContextHolder = where Spring keeps "req.user" (thread-local, per request).
 *
 * Note what this filter does NOT do: it never rejects a request. It only attaches
 * the user when the token is valid. The DECISION to reject (401) happens later,
 * in the rules declared in SecurityConfig.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserRepository users;

    public JwtAuthFilter(JwtService jwtService, AppUserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // strip "Bearer "
            try {
                String email = jwtService.validateAndGetSubject(token);
                users.findByEmail(email).ifPresent(user -> {
                    // "ROLE_" prefix is a Spring Security convention — it's what lets us
                    // later write rules like .hasRole("SUPERVISOR").
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user.getEmail(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            } catch (JwtException e) {
                // Bad/expired token: attach nothing. The request proceeds as anonymous
                // and SecurityConfig's rules will 401 it if the route needs auth.
            }
        }

        filterChain.doFilter(request, response); // next()
    }
}
