package com.blooddonation.spring.repository;

import com.blooddonation.spring.model.UserAccount;
import com.blooddonation.spring.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findByUsername(String username);
    List<UserAccount> findByRole(UserRole role);
    boolean existsByUsername(String username);
}
Commit at 2026-08-27T09:28:16
Commit at 2026-08-16T04:44:50
Commit at 2026-08-04T10:17:35
Commit at 2026-09-15T13:21:43
Commit at 2026-08-08T19:01:37
Commit at 2026-08-23T07:40:37
