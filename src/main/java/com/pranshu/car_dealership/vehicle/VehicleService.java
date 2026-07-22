package com.pranshu.car_dealership.vehicle;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository repository;

    public VehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    public Vehicle create(Vehicle vehicle) {
        return repository.save(vehicle);
    }

    public List<Vehicle> findAll() {
        return repository.findAll();
    }

    public List<Vehicle> search(String make, String model, String category,
                                BigDecimal minPrice, BigDecimal maxPrice) {
        return repository.search(make, model, category, minPrice, maxPrice);
    }

    @Transactional
    public Vehicle purchase(Long id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Purchase quantity must be positive, but was " + quantity);
        }
        Vehicle vehicle = repository.findById(id).orElseThrow(() -> new VehicleNotFoundException(id));

        if (vehicle.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Cannot purchase " + quantity + " units of vehicle " + id
                            + "; only " + vehicle.getQuantity() + " available");
        }

        vehicle.setQuantity(vehicle.getQuantity() - quantity);
        return repository.save(vehicle);
    }
}