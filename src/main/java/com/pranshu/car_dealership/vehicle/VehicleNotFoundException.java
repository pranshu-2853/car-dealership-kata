package com.pranshu.car_dealership.vehicle;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(Long id) {
        super("Vehicle not found with id " + id);
    }
}
