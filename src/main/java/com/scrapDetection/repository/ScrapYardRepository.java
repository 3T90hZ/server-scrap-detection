package com.scrapDetection.repository;

import com.scrapDetection.entity.ScrapYard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapYardRepository extends JpaRepository<ScrapYard, Long> {

    List<ScrapYard> findByStatus(String status);

    Page<ScrapYard> findByStatus(String status, Pageable pageable);

    boolean existsByPhoneNumbers(String phoneNumbers);

    boolean existsByAddress(String address);

    // boolean existsByPhoneNumbersAndYardIdNot(String phoneNumbers, Long yardId);

    boolean existsByYardName(String yardName);

    Optional<ScrapYard> findByYardName(String yardName);

    List<ScrapYard> findByYardNameContainingIgnoreCase(String yardName);
}
