package com.pranshu.car_dealership.vehicle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @Test
    void createsVehicleAndReturns201() throws Exception {
        when(vehicleService.create(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle toSave = invocation.getArgument(0);
            toSave.setId(1L);
            return toSave;
        });

        String requestBody = """
                {
                  "make": "Toyota",
                  "model": "Corolla",
                  "category": "Sedan",
                  "price": 1850000.00,
                  "quantity": 4
                }
                """;

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.category").value("Sedan"))
                .andExpect(jsonPath("$.price").value(1850000.00))
                .andExpect(jsonPath("$.quantity").value(4));
    }

    @Test
    void returnsAllVehicles() throws Exception {
        when(vehicleService.findAll()).thenReturn(List.of(
                vehicle(1L, "Toyota", "Corolla", "Sedan", "1850000.00", 4),
                vehicle(2L, "Honda", "City", "Sedan", "1400000.00", 2)));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].make").value("Toyota"))
                .andExpect(jsonPath("$[0].model").value("Corolla"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].make").value("Honda"))
                .andExpect(jsonPath("$[1].model").value("City"));
    }

    @Test
    void returnsEmptyListWhenNoVehiclesExist() throws Exception {
        when(vehicleService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private Vehicle vehicle(Long id, String make, String model, String category, String price, int quantity) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setMake(make);
        vehicle.setModel(model);
        vehicle.setCategory(category);
        vehicle.setPrice(new BigDecimal(price));
        vehicle.setQuantity(quantity);
        return vehicle;
    }
}
