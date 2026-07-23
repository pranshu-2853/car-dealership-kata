package com.pranshu.car_dealership.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pranshu.car_dealership.auth.AuthDtos.LoginResponse;
import com.pranshu.car_dealership.web.GlobalExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registersUserAndReturns201WithoutLeakingThePassword() throws Exception {
        User registered = new User();
        registered.setId(1L);
        registered.setUsername("pranshu");
        registered.setPassword("$2a$10$someBcryptHashThatMustNeverBeSerialised");
        registered.setRole(Role.USER);

        when(authService.register("pranshu", "secret123")).thenReturn(registered);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "pranshu", "password": "secret123" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("pranshu"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void returns409WhenUsernameAlreadyTaken() throws Exception {
        when(authService.register("pranshu", "secret123"))
                .thenThrow(new UsernameAlreadyExistsException("pranshu"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "pranshu", "password": "secret123" }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already taken: pranshu"));
    }

    @Test
    void returns400WhenUsernameIsBlank() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "", "password": "secret123" }
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, org.mockito.Mockito.never()).register(anyString(), anyString());
    }

    @Test
    void loginReturns200WithToken() throws Exception {
        when(authService.login("pranshu", "secret123"))
                .thenReturn(new LoginResponse("a.jwt.token", "pranshu", Role.ADMIN));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "pranshu", "password": "secret123" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("a.jwt.token"))
                .andExpect(jsonPath("$.username").value("pranshu"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void loginReturns401WhenCredentialsAreInvalid() throws Exception {
        when(authService.login("pranshu", "wrong-password")).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "pranshu", "password": "wrong-password" }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}
