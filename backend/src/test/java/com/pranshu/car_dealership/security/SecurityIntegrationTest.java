package com.pranshu.car_dealership.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import com.pranshu.car_dealership.auth.JwtService;
import com.pranshu.car_dealership.auth.Role;
import com.pranshu.car_dealership.auth.User;
import com.pranshu.car_dealership.auth.UserRepository;
import com.pranshu.car_dealership.vehicle.Vehicle;
import com.pranshu.car_dealership.vehicle.VehicleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String adminToken;
    private Long vehicleId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        vehicleRepository.deleteAll();

        userToken = jwtService.generateToken(persistUser("normal", Role.USER).getUsername(), Role.USER);
        adminToken = jwtService.generateToken(persistUser("boss", Role.ADMIN).getUsername(), Role.ADMIN);

        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setCategory("Sedan");
        vehicle.setPrice(new BigDecimal("1850000.00"));
        vehicle.setQuantity(5);
        vehicleId = vehicleRepository.save(vehicle).getId();
    }

    @Test
    void rejectsUnauthenticatedRequestWith401() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAuthenticatedUserToListVehicles() throws Exception {
        mockMvc.perform(get("/api/vehicles").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void forbidsNonAdminFromDeletingWith403() throws Exception {
        mockMvc.perform(delete("/api/vehicles/" + vehicleId).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminToDeleteWith204() throws Exception {
        mockMvc.perform(delete("/api/vehicles/" + vehicleId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void forbidsNonAdminFromRestockingWith403() throws Exception {
        mockMvc.perform(post("/api/vehicles/" + vehicleId + "/restock")
                        .param("quantity", "3")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminToRestockWith200() throws Exception {
        mockMvc.perform(post("/api/vehicles/" + vehicleId + "/restock")
                        .param("quantity", "3")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void allowsAnyAuthenticatedUserToPurchase() throws Exception {
        mockMvc.perform(post("/api/vehicles/" + vehicleId + "/purchase")
                        .param("quantity", "1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsRequestBearingATamperedTokenWith401() throws Exception {
        mockMvc.perform(get("/api/vehicles").header("Authorization", "Bearer " + adminToken + "tampered"))
                .andExpect(status().isUnauthorized());
    }

    private User persistUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("irrelevant-for-token-auth"));
        user.setRole(role);
        return userRepository.save(user);
    }
}
