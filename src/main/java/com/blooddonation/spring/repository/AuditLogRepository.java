package com.blooddonation.spring.repository;

import com.blooddonation.spring.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
}
Commit at 2026-10-02T12:06:02
Commit at 2026-10-04T10:54:33
Commit at 2026-10-02T19:50:38
Commit at 2026-09-25T04:35:13
Commit at 2026-10-02T20:26:15
Commit at 2026-09-30T20:13:06
