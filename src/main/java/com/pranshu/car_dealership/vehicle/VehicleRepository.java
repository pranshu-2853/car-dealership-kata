package com.pranshu.car_dealership.vehicle;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // The casts below are required for PostgreSQL; H2 tolerates untyped nulls, which is
    // why the H2 test never caught either issue. A blank filter is bound as a null with no
    // type, and Postgres surfaces it two ways:
    //   1) Parse time: a bare ":param IS NULL" cannot be typed -> "could not determine data
    //      type of parameter". An explicit cast on every occurrence gives it a concrete type.
    //   2) Execution time: the driver sends the null as an unspecified binary the server
    //      treats as bytea. "bytea -> varchar" is legal, so the String params are fine, but
    //      "bytea -> numeric" is NOT ("cannot cast type bytea to numeric"). Routing the
    //      numeric params through text first (bytea -> varchar -> numeric) is legal, hence
    //      the nested CAST(CAST(:param AS String) AS BigDecimal).
    @Query("""
            SELECT v FROM Vehicle v
            WHERE (CAST(:make AS String) IS NULL OR LOWER(v.make) = LOWER(CAST(:make AS String)))
              AND (CAST(:model AS String) IS NULL OR LOWER(v.model) = LOWER(CAST(:model AS String)))
              AND (CAST(:category AS String) IS NULL OR LOWER(v.category) = LOWER(CAST(:category AS String)))
              AND (CAST(CAST(:minPrice AS String) AS BigDecimal) IS NULL OR v.price >= CAST(CAST(:minPrice AS String) AS BigDecimal))
              AND (CAST(CAST(:maxPrice AS String) AS BigDecimal) IS NULL OR v.price <= CAST(CAST(:maxPrice AS String) AS BigDecimal))
            """)
    List<Vehicle> search(@Param("make") String make,
                         @Param("model") String model,
                         @Param("category") String category,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice);
}
