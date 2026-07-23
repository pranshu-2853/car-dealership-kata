package com.pranshu.car_dealership.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request and response shapes for the auth endpoints. Kept together because they
 * are small, cohesive, and only meaningful as a set.
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    /** Deliberately omits the password hash — never leaves the server. */
    public record UserResponse(Long id, String username, Role role) {
    }

    public record LoginResponse(String token, String username, Role role) {
    }
}
