package com.pranshu.car_dealership.vehicle;

import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    private final VehicleRepository repository;

    public VehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    public Vehicle create(Vehicle vehicle) {
        return repository.save(vehicle);
    }
}