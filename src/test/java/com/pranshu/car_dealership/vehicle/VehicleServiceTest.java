package com.pranshu.car_dealership.vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void createsVehicleWithGeneratedId() {
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle toSave = invocation.getArgument(0);
            toSave.setId(1L);
            return toSave;
        });

        Vehicle created = vehicleService.create(
                new Vehicle("Toyota", "Corolla", "Sedan", new BigDecimal("1850000.00"), 4));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getMake()).isEqualTo("Toyota");
        assertThat(created.getModel()).isEqualTo("Corolla");
        assertThat(created.getCategory()).isEqualTo("Sedan");
        assertThat(created.getPrice()).isEqualByComparingTo("1850000.00");
        assertThat(created.getQuantity()).isEqualTo(4);
    }
}
