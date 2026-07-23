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

    @Test
    void rejectsNonPositivePurchaseQuantity() {
        assertThatThrownBy(() -> vehicleService.purchase(1L, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> vehicleService.purchase(1L, -3))
                .isInstanceOf(IllegalArgumentException.class);

        verify(vehicleRepository, never()).findById(any());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void updatesAllEditableFieldsButKeepsTheOriginalId() {
        Vehicle existing = new Vehicle();
        existing.setId(1L);
        existing.setMake("Toyota");
        existing.setModel("Corolla");
        existing.setCategory("Sedan");
        existing.setPrice(new BigDecimal("1850000.00"));
        existing.setQuantity(5);

        Vehicle updated = new Vehicle();
        updated.setId(999L);
        updated.setMake("Honda");
        updated.setModel("City");
        updated.setCategory("Hatchback");
        updated.setPrice(new BigDecimal("1400000.00"));
        updated.setQuantity(2);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle result = vehicleService.update(1L, updated);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMake()).isEqualTo("Honda");
        assertThat(result.getModel()).isEqualTo("City");
        assertThat(result.getCategory()).isEqualTo("Hatchback");
        assertThat(result.getPrice()).isEqualByComparingTo("1400000.00");
        assertThat(result.getQuantity()).isEqualTo(2);
    }

    @Test
    void throwsWhenUpdatingVehicleThatDoesNotExist() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.update(99L, new Vehicle()))
                .isInstanceOf(VehicleNotFoundException.class);

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void deletesExistingVehicle() {
        Vehicle existing = new Vehicle();
        existing.setId(1L);
        existing.setMake("Toyota");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing));

        vehicleService.delete(1L);

        verify(vehicleRepository).delete(existing);
    }

    @Test
    void throwsWhenDeletingVehicleThatDoesNotExist() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.delete(99L))
                .isInstanceOf(VehicleNotFoundException.class);

        verify(vehicleRepository, never()).delete(any(Vehicle.class));
    }

    @Test
    void increasesQuantityByRestockedAmount() {
        Vehicle existing = new Vehicle();
        existing.setId(1L);
        existing.setMake("Toyota");
        existing.setModel("Corolla");
        existing.setCategory("Sedan");
        existing.setPrice(new BigDecimal("1850000.00"));
        existing.setQuantity(5);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle restocked = vehicleService.restock(1L, 3);

        assertThat(restocked.getQuantity()).isEqualTo(8);

        ArgumentCaptor<Vehicle> saved = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(saved.capture());
        assertThat(saved.getValue().getQuantity()).isEqualTo(8);
    }

    @Test
    void rejectsNonPositiveRestockQuantity() {
        assertThatThrownBy(() -> vehicleService.restock(1L, 0))
                .isInstanceOf(IllegalArgumentException.class);

        verify(vehicleRepository, never()).findById(any());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void throwsWhenRestockingVehicleThatDoesNotExist() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.restock(99L, 1))
                .isInstanceOf(VehicleNotFoundException.class);

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }
}
