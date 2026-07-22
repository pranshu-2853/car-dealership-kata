package com.pranshu.car_dealership.vehicle;

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

    public Vehicle purchase(Long id, int quantity) {
        Vehicle vehicle = repository.findById(id).orElseThrow();
        vehicle.setQuantity(vehicle.getQuantity() - quantity);
        return repository.save(vehicle);
    }
}