package com.scrapDetection.repository;

import com.scrapDetection.entity.Resale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ResaleRepository extends JpaRepository<Resale, Long> {

    List<Resale> findByMaterialScrapYardYardId(Long yardId);

    List<Resale> findByCreatedByAccountId(Long staffId);

    List<Resale> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}