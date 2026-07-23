package com.pranshu.car_dealership.vehicle;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // CAST(:param AS ...) is required for PostgreSQL. A blank filter is bound as an
    // untyped null; Postgres infers parameter types at parse time and cannot type a bare
    // ":param IS NULL", so the whole statement fails with "could not determine data type
    // of parameter". An explicit HQL cast gives every occurrence a concrete type. H2 never
    // hit this because it tolerates untyped-null inference, which is why the H2 test passed.
    @Query("""
            SELECT v FROM Vehicle v
            WHERE (CAST(:make AS String) IS NULL OR LOWER(v.make) = LOWER(CAST(:make AS String)))
              AND (CAST(:model AS String) IS NULL OR LOWER(v.model) = LOWER(CAST(:model AS String)))
              AND (CAST(:category AS String) IS NULL OR LOWER(v.category) = LOWER(CAST(:category AS String)))
              AND (CAST(:minPrice AS BigDecimal) IS NULL OR v.price >= CAST(:minPrice AS BigDecimal))
              AND (CAST(:maxPrice AS BigDecimal) IS NULL OR v.price <= CAST(:maxPrice AS BigDecimal))
            """)
    List<Vehicle> search(@Param("make") String make,
                         @Param("model") String model,
                         @Param("category") String category,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice);
}
