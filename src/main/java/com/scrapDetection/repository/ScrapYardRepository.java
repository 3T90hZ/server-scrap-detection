package com.scrapDetection.repository;

import com.scrapDetection.entity.ScrapYard;
import com.scrapDetection.entity.YardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapYardRepository extends JpaRepository<ScrapYard, Long> {

    Page<ScrapYard> findByStatus(YardStatus status, Pageable pageable);

    boolean existsByPhoneNumbers(String phoneNumbers);

    Optional<ScrapYard> findByPhoneNumbers(String phoneNumbers);

    boolean existsByAddress(String address);

    boolean existsByYardNameIgnoreCase(String yardName);

    Optional<ScrapYard> findByYardName(String yardName);

    List<ScrapYard> findByYardNameContainingIgnoreCase(String yardName);
}
