package com.pranshu.car_dealership.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import com.pranshu.car_dealership.auth.JwtService;
import com.pranshu.car_dealership.auth.Role;
import com.pranshu.car_dealership.auth.User;
import com.pranshu.car_dealership.auth.UserRepository;
import com.pranshu.car_dealership.vehicle.Vehicle;
import com.pranshu.car_dealership.vehicle.VehicleRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Runs against the real embedded Tomcat (RANDOM_PORT), so requests traverse the
 * servlet container's ERROR dispatch — the path MockMvc skips. This is the only
 * test that exercises what happens when the security chain denies a request and
 * Tomcat re-dispatches to /error, which is where the 403-rewritten-to-401 bug lived.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityErrorDispatchIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private Long vehicleId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        vehicleRepository.deleteAll();

        User user = new User();
        user.setUsername("normal");
        user.setPassword(passwordEncoder.encode("irrelevant-for-token-auth"));
        user.setRole(Role.USER);
        userRepository.save(user);
        userToken = jwtService.generateToken(user.getUsername(), Role.USER);

        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setCategory("Sedan");
        vehicle.setPrice(new BigDecimal("1850000.00"));
        vehicle.setQuantity(5);
        vehicleId = vehicleRepository.save(vehicle).getId();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    @Test
    void nonAdminDeleteReturns403ThroughTheRealErrorDispatch() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/vehicles/" + vehicleId, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
