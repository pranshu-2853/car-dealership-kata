package com.pranshu.car_dealership.vehicle;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("""
            SELECT v FROM Vehicle v
            WHERE (:make IS NULL OR LOWER(v.make) = LOWER(:make))
              AND (:model IS NULL OR LOWER(v.model) = LOWER(:model))
              AND (:category IS NULL OR LOWER(v.category) = LOWER(:category))
              AND (:minPrice IS NULL OR v.price >= :minPrice)
              AND (:maxPrice IS NULL OR v.price <= :maxPrice)
            """)
    List<Vehicle> search(@Param("make") String make,
                         @Param("model") String model,
                         @Param("category") String category,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice);
}
