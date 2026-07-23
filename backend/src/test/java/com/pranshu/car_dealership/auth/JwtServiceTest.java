package com.pranshu.car_dealership.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hs256-signing";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000L);

    @Test
    void generatesTokenCarryingUsernameAndRole() {
        String token = jwtService.generateToken("pranshu", Role.ADMIN);

        assertThat(jwtService.extractUsername(token)).isEqualTo("pranshu");
        assertThat(jwtService.extractRole(token)).isEqualTo(Role.ADMIN);
    }

    @Test
    void rejectsTokenSignedWithADifferentSecret() {
        JwtService attacker = new JwtService("a-completely-different-secret-key-of-sufficient-length", 3_600_000L);
        String forgedToken = attacker.generateToken("pranshu", Role.ADMIN);

        assertThatThrownBy(() -> jwtService.extractUsername(forgedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() {
        JwtService alreadyExpired = new JwtService(SECRET, -1_000L);
        String expiredToken = alreadyExpired.generateToken("pranshu", Role.USER);

        assertThatThrownBy(() -> jwtService.extractUsername(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
