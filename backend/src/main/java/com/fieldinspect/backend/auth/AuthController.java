package com.fieldinspect.backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fieldinspect.backend.user.AppUser;
import com.fieldinspect.backend.user.AppUserRepository;

import jakarta.validation.Valid;

/**
 * POST /api/auth/login  { "email": ..., "password": ... }  ->  { "token": ... }
 *
 * Two new pieces:
 *   @RequestBody  -> parse the JSON body into a LoginRequest (Express: req.body,
 *                    but typed — wrong shape can't even get in).
 *   @Valid        -> run the annotations declared on LoginRequest first;
 *                    failures become a 400 automatically.
 *   ResponseEntity -> full control of the response (status + body) when we need
 *                    something other than a plain 200, like Express's res.status(401).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AppUserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest body) {
        AppUser user = users.findByEmail(body.email()).orElse(null);

        // Same 401 whether the email is unknown OR the password is wrong —
        // never reveal which emails exist to someone probing the API.
        if (user == null || !passwordEncoder.matches(body.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.issueToken(user);
        return ResponseEntity.ok(new LoginResponse(token, user.getFullName(), user.getRole()));
    }
}
