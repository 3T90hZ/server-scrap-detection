package com.scrapDetection.repository;

import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountName(String accountName);

    boolean existsByAccountName(String accountName);

    List<Account> findByRole(Role role);

    List<Account> findByYardId(Long yardId);

    List<Account> findByStatus(String status);
}
