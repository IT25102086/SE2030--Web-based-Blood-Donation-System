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
Commit at 2026-10-01T05:10:46
Commit at 2026-09-25T09:40:09
Commit at 2026-09-29T19:47:07
Commit at 2026-10-02T01:42:21
Commit at 2026-10-04T17:18:10
Commit at 2026-10-02T12:57:37
Commit at 2026-10-04T07:27:49
Commit at 2026-10-05T02:28:31
Commit at 2026-10-04T14:49:49
Commit at 2026-09-29T21:30:56
Commit at 2026-09-25T19:55:53
Commit at 2026-09-27T17:13:43
Commit at 2026-10-04T09:38:02
Commit at 2026-10-05T07:36:11
Commit at 2026-09-28T10:37:31
Commit at 2026-09-26T03:02:07
Commit at 2026-09-25T16:11:22
Commit at 2026-10-02T06:47:43
Commit at 2026-09-28T00:23:44
Commit at 2026-09-30T18:59:04
Commit at 2026-10-03T09:46:50
