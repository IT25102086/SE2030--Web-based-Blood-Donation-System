package com.blooddonation.spring.repository;

import com.blooddonation.spring.model.BloodUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BloodUnitRepository extends JpaRepository<BloodUnit, String> {
    List<BloodUnit> findByStatus(String status);

    // Real-world: DB-level counts + expiry queries
    long countByStatus(String status);

    long countByStatusAndBloodGroupIgnoreCaseAndExpiresAtAfter(String status, String bloodGroup, java.time.LocalDateTime now);

    List<BloodUnit> findByStatusAndExpiresAtBefore(String status, java.time.LocalDateTime now);

    List<BloodUnit> findByStatusAndExpiresAtAfter(String status, java.time.LocalDateTime now);
}
