package com.blooddonation.spring.repository;

import com.blooddonation.spring.model.BloodUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BloodUnitRepository extends JpaRepository<BloodUnit, String> {
    List<BloodUnit> findByStatus(String status);
}
