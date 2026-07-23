package com.pranshu.car_dealership.vehicle;

import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public Vehicle update(Long id, Vehicle updated) {
        Vehicle existing = findOrThrow(id);

        existing.setMake(updated.getMake());
        existing.setModel(updated.getModel());
        existing.setCategory(updated.getCategory());
        existing.setPrice(updated.getPrice());
        existing.setQuantity(updated.getQuantity());

        return repository.save(existing);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        repository.delete(findOrThrow(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Vehicle restock(Long id, int quantity) {
        requirePositive(quantity, "Restock");
        Vehicle vehicle = findOrThrow(id);

        vehicle.setQuantity(vehicle.getQuantity() + quantity);
        return repository.save(vehicle);
    }

    @Transactional
    public Vehicle purchase(Long id, int quantity) {
        requirePositive(quantity, "Purchase");
        Vehicle vehicle = findOrThrow(id);

        if (vehicle.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Cannot purchase " + quantity + " units of vehicle " + id
                            + "; only " + vehicle.getQuantity() + " available");
        }

        vehicle.setQuantity(vehicle.getQuantity() - quantity);
        return repository.save(vehicle);
    }

    private Vehicle findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new VehicleNotFoundException(id));
    }

    private void requirePositive(int quantity, String operation) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(operation + " quantity must be positive, but was " + quantity);
        }
    }
}