package com.fieldinspect.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * NEW CONCEPT — a record: one line declares an immutable class with constructor,
 * getters (email(), password()), equals/hashCode/toString. The closest Java gets
 * to `type LoginRequest = { email: string; password: string }`.
 *
 * The validation annotations run because the controller marks this @Valid:
 * a blank or non-email value -> automatic 400 Bad Request before our code runs.
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
