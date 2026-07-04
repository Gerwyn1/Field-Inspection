package com.fieldinspect.backend.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fieldinspect.backend.user.AppUser;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Issues and verifies JWTs. This is our wrapper around the jjwt library —
 * the equivalent of the two calls you know from Node:
 *
 *   jwt.sign({ sub: email, role }, SECRET, { expiresIn: '60m' })   -> issueToken(...)
 *   jwt.verify(token, SECRET).sub                                  -> validateAndGetSubject(...)
 *
 * @Service is just @Component with a clearer name ("business logic lives here").
 * @Value("${app.jwt.secret}") injects a value from application.properties —
 * like process.env.JWT_SECRET, but typed and validated at startup.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        // Turn the secret string into an HMAC-SHA256 signing key.
        // jjwt throws at startup if the secret is shorter than 32 bytes — fail fast.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    /** Build a signed token. "subject" is the JWT-standard field for "who is this". */
    public String issueToken(AppUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole())
                .claim("name", user.getFullName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /**
     * Verify signature + expiry, then return the subject (the email).
     * Throws JwtException for anything wrong: bad signature, expired, malformed.
     */
    public String validateAndGetSubject(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
