package com.pranshu.car_dealership.vehicle;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class VehicleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void findsVehiclesByMake() {
        entityManager.persist(vehicle("Toyota", "Corolla", "Sedan", "1850000.00", 4));
        entityManager.persist(vehicle("Toyota", "Fortuner", "SUV", "4200000.00", 2));
        entityManager.persist(vehicle("Honda", "City", "Sedan", "1400000.00", 3));
        entityManager.flush();

        List<Vehicle> results = vehicleRepository.search("Toyota", null, null, null, null);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Vehicle::getMake).containsOnly("Toyota");
        assertThat(results).extracting(Vehicle::getModel)
                .containsExactlyInAnyOrder("Corolla", "Fortuner");
    }

    @Test
    void findsVehiclesByModelAndCategoryCombined() {
        entityManager.persist(vehicle("Toyota", "Corolla", "Sedan", "1850000.00", 4));
        entityManager.persist(vehicle("Toyota", "Corolla", "Hatchback", "1600000.00", 1));
        entityManager.persist(vehicle("Honda", "City", "Sedan", "1400000.00", 3));
        entityManager.flush();

        List<Vehicle> byModelOnly = vehicleRepository.search(null, "Corolla", null, null, null);
        List<Vehicle> byModelAndCategory = vehicleRepository.search(null, "Corolla", "Sedan", null, null);

        assertThat(byModelOnly).hasSize(2);
        assertThat(byModelOnly).extracting(Vehicle::getModel).containsOnly("Corolla");

        assertThat(byModelAndCategory).hasSize(1);
        assertThat(byModelAndCategory).first()
                .satisfies(found -> {
                    assertThat(found.getModel()).isEqualTo("Corolla");
                    assertThat(found.getCategory()).isEqualTo("Sedan");
                });
    }

    @Test
    void findsVehiclesWithinPriceRangeInclusiveOfBounds() {
        entityManager.persist(vehicle("Honda", "Amaze", "Sedan", "900000.00", 5));
        entityManager.persist(vehicle("Honda", "City", "Sedan", "1000000.00", 3));
        entityManager.persist(vehicle("Toyota", "Corolla", "Sedan", "1500000.00", 4));
        entityManager.persist(vehicle("Toyota", "Fortuner", "SUV", "2000000.00", 2));
        entityManager.persist(vehicle("BMW", "X5", "SUV", "9000000.00", 1));
        entityManager.flush();

        List<Vehicle> results = vehicleRepository.search(
                null, null, null, new BigDecimal("1000000.00"), new BigDecimal("2000000.00"));

        assertThat(results).extracting(Vehicle::getModel)
                .containsExactlyInAnyOrder("City", "Corolla", "Fortuner");
    }

    @Test
    void returnsAllVehiclesWhenNoFiltersGiven() {
        entityManager.persist(vehicle("Toyota", "Corolla", "Sedan", "1850000.00", 4));
        entityManager.persist(vehicle("Honda", "City", "Sedan", "1400000.00", 3));
        entityManager.persist(vehicle("BMW", "X5", "SUV", "9000000.00", 1));
        entityManager.flush();

        List<Vehicle> results = vehicleRepository.search(null, null, null, null, null);

        assertThat(results).hasSize(3);
    }

    private Vehicle vehicle(String make, String model, String category, String price, int quantity) {
        Vehicle vehicle = new Vehicle();
        vehicle.setMake(make);
        vehicle.setModel(model);
        vehicle.setCategory(category);
        vehicle.setPrice(new BigDecimal(price));
        vehicle.setQuantity(quantity);
        return vehicle;
    }
}
