package com.pranshu.car_dealership.vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import com.pranshu.car_dealership.web.GlobalExceptionHandler;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VehicleController.class)
@Import(GlobalExceptionHandler.class)
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

    @Test
    void passesSuppliedSearchParamsToServiceAndLeavesOmittedOnesNull() throws Exception {
        when(vehicleService.search(any(), any(), any(), any(), any()))
                .thenReturn(List.of(vehicle(1L, "Toyota", "Corolla", "Sedan", "1850000.00", 4)));

        mockMvc.perform(get("/api/vehicles/search")
                        .param("make", "Toyota")
                        .param("minPrice", "1000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].make").value("Toyota"));

        ArgumentCaptor<BigDecimal> minPrice = ArgumentCaptor.forClass(BigDecimal.class);
        verify(vehicleService).search(
                eq("Toyota"), isNull(), isNull(), minPrice.capture(), isNull());
        assertThat(minPrice.getValue()).isEqualByComparingTo("1000000");
    }

    @Test
    void purchasesVehicleAndReturns200() throws Exception {
        when(vehicleService.purchase(1L, 2))
                .thenReturn(vehicle(1L, "Toyota", "Corolla", "Sedan", "1850000.00", 3));

        mockMvc.perform(post("/api/vehicles/1/purchase").param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(3));

        verify(vehicleService).purchase(1L, 2);
    }

    @Test
    void defaultsPurchaseQuantityToOneWhenOmitted() throws Exception {
        when(vehicleService.purchase(1L, 1))
                .thenReturn(vehicle(1L, "Toyota", "Corolla", "Sedan", "1850000.00", 4));

        mockMvc.perform(post("/api/vehicles/1/purchase"))
                .andExpect(status().isOk());

        verify(vehicleService).purchase(1L, 1);
    }

    @Test
    void returns409WhenStockIsInsufficient() throws Exception {
        when(vehicleService.purchase(1L, 10))
                .thenThrow(new InsufficientStockException(
                        "Cannot purchase 10 units of vehicle 1; only 5 available"));

        mockMvc.perform(post("/api/vehicles/1/purchase").param("quantity", "10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Cannot purchase 10 units of vehicle 1; only 5 available"));
    }

    @Test
    void returns404WhenVehicleDoesNotExist() throws Exception {
        when(vehicleService.purchase(99L, 1)).thenThrow(new VehicleNotFoundException(99L));

        mockMvc.perform(post("/api/vehicles/99/purchase"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle not found with id 99"));
    }

    @Test
    void returns400WhenPurchaseQuantityIsNotPositive() throws Exception {
        when(vehicleService.purchase(1L, 0))
                .thenThrow(new IllegalArgumentException("Purchase quantity must be positive, but was 0"));

        mockMvc.perform(post("/api/vehicles/1/purchase").param("quantity", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Purchase quantity must be positive, but was 0"));
    }

    @Test
    void updatesVehicleAndReturns200() throws Exception {
        when(vehicleService.update(eq(1L), any(Vehicle.class)))
                .thenReturn(vehicle(1L, "Honda", "City", "Hatchback", "1400000.00", 2));

        String requestBody = """
                {
                  "make": "Honda",
                  "model": "City",
                  "category": "Hatchback",
                  "price": 1400000.00,
                  "quantity": 2
                }
                """;

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.make").value("Honda"))
                .andExpect(jsonPath("$.quantity").value(2));

        ArgumentCaptor<Vehicle> submitted = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleService).update(eq(1L), submitted.capture());
        assertThat(submitted.getValue().getMake()).isEqualTo("Honda");
        assertThat(submitted.getValue().getQuantity()).isEqualTo(2);
    }

    @Test
    void deletesVehicleAndReturns204() throws Exception {
        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(vehicleService).delete(1L);
    }

    @Test
    void restocksVehicleAndReturns200() throws Exception {
        when(vehicleService.restock(1L, 3))
                .thenReturn(vehicle(1L, "Toyota", "Corolla", "Sedan", "1850000.00", 8));

        mockMvc.perform(post("/api/vehicles/1/restock").param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(8));

        verify(vehicleService).restock(1L, 3);
    }

    @Test
    void returns404WhenRestockingVehicleThatDoesNotExist() throws Exception {
        when(vehicleService.restock(99L, 3)).thenThrow(new VehicleNotFoundException(99L));

        mockMvc.perform(post("/api/vehicles/99/restock").param("quantity", "3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle not found with id 99"));
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
