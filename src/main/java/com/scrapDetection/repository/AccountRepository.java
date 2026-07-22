package com.scrapDetection.repository;

import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Primary login method - Find by Phone Number
    Optional<Account> findByPhoneNumbers(String phoneNumbers);

    // Uniqueness checks
    boolean existsByPhoneNumbers(String phoneNumbers);

    boolean existsByEmail(String email);

    List<Account> findByScrapYardYardIdAndRole(Long yardId, Role role);

    // For password reset
    Optional<Account> findByEmail(String email);
}