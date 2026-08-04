package com.scrapDetection.repository;

import com.scrapDetection.entity.PasswordResetToken;
import com.scrapDetection.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByAccount(Account account);
}