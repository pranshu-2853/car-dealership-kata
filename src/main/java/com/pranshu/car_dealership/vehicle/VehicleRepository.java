package com.pranshu.car_dealership.vehicle;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("""
            SELECT v FROM Vehicle v
            WHERE (:make IS NULL OR v.make = :make)
            """)
    List<Vehicle> search(@Param("make") String make,
                         @Param("model") String model,
                         @Param("category") String category,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice);
}
