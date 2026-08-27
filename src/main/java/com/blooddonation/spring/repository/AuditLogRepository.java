package com.blooddonation.spring.repository;

import com.blooddonation.spring.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
}
Commit at 2026-08-27T09:28:16
