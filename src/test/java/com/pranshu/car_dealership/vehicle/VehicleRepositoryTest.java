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
