package com.pranshu.car_dealership.vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

        Vehicle toCreate = new Vehicle();
        toCreate.setMake("Toyota");
        toCreate.setModel("Corolla");
        toCreate.setCategory("Sedan");
        toCreate.setPrice(new BigDecimal("1850000.00"));
        toCreate.setQuantity(4);

        Vehicle created = vehicleService.create(toCreate);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getMake()).isEqualTo("Toyota");
        assertThat(created.getModel()).isEqualTo("Corolla");
        assertThat(created.getCategory()).isEqualTo("Sedan");
        assertThat(created.getPrice()).isEqualByComparingTo("1850000.00");
        assertThat(created.getQuantity()).isEqualTo(4);
    }

    @Test
    void decreasesQuantityByPurchasedAmount() {
        Vehicle existing = new Vehicle();
        existing.setId(1L);
        existing.setMake("Toyota");
        existing.setModel("Corolla");
        existing.setCategory("Sedan");
        existing.setPrice(new BigDecimal("1850000.00"));
        existing.setQuantity(5);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle purchased = vehicleService.purchase(1L, 2);

        assertThat(purchased.getQuantity()).isEqualTo(3);

        ArgumentCaptor<Vehicle> saved = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(saved.capture());
        assertThat(saved.getValue().getQuantity()).isEqualTo(3);
    }

    @Test
    void throwsWhenStockIsInsufficient() {
        Vehicle existing = new Vehicle();
        existing.setId(1L);
        existing.setMake("Toyota");
        existing.setModel("Corolla");
        existing.setCategory("Sedan");
        existing.setPrice(new BigDecimal("1850000.00"));
        existing.setQuantity(5);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> vehicleService.purchase(1L, 10))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("5");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
        assertThat(existing.getQuantity()).isEqualTo(5);
    }

    @Test
    void throwsWhenVehicleDoesNotExist() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.purchase(99L, 1))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("99");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }
}
