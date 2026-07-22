package com.pranshu.car_dealership.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pranshu.car_dealership.auth.AuthDtos.LoginResponse;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final JwtService jwtService =
            new JwtService("test-secret-key-that-is-long-enough-for-hs256-signing", 3_600_000L);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registersUserWithHashedPassword() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register("pranshu", "secret123");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        User registered = saved.getValue();

        assertThat(registered.getUsername()).isEqualTo("pranshu");
        assertThat(registered.getPassword()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", registered.getPassword())).isTrue();
        assertThat(registered.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void rejectsRegistrationWhenUsernameAlreadyTaken() {
        when(userRepository.existsByUsername("pranshu")).thenReturn(true);

        assertThatThrownBy(() -> authService.register("pranshu", "secret123"))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("pranshu");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginReturnsTokenCarryingTheUsersRole() {
        when(userRepository.findByUsername("pranshu")).thenReturn(Optional.of(existingUser("pranshu", "secret123", Role.ADMIN)));

        LoginResponse response = authService.login("pranshu", "secret123");

        assertThat(response.username()).isEqualTo("pranshu");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
        assertThat(jwtService.extractUsername(response.token())).isEqualTo("pranshu");
        assertThat(jwtService.extractRole(response.token())).isEqualTo(Role.ADMIN);
    }

    @Test
    void loginRejectsWrongPassword() {
        when(userRepository.findByUsername("pranshu")).thenReturn(Optional.of(existingUser("pranshu", "secret123", Role.USER)));

        assertThatThrownBy(() -> authService.login("pranshu", "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginRejectsUnknownUsernameWithTheSameErrorAsAWrongPassword() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("nobody", "secret123"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    private User existingUser(String username, String rawPassword, Role role) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return user;
    }
}
