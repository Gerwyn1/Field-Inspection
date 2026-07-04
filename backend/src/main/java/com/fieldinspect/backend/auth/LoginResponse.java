package com.fieldinspect.backend.auth;

/** What a successful login returns — the token plus enough info to greet the user. */
public record LoginResponse(String token, String fullName, String role) {
}
