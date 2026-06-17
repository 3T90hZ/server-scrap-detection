package com.scrapDetection.repository;

import com.scrapDetection.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByJwtTokenHash(String jwtTokenHash);

    List<Session> findByAccountAccountId(Long accountId);

    void deleteByExpiredAtBefore(LocalDateTime now);

    void deleteByAccountAccountId(Long accountId);

}
