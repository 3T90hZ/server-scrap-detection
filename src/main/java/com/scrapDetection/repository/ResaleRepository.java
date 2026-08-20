package com.scrapDetection.repository;

import com.scrapDetection.entity.Resale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ResaleRepository extends JpaRepository<Resale, Long> {

    List<Resale> findByMaterialScrapYardYardId(Long yardId);

    List<Resale> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // ----- Statistics -----
    @EntityGraph(attributePaths = {"resaleTotal", "material"})
    @Query("""
            SELECT r FROM Resale r
            WHERE r.material.scrapYard.yardId = :yardId
              AND (:startDate IS NULL OR r.createdAt >= :startDate)
              AND (:endDate IS NULL OR r.createdAt <= :endDate)
            ORDER BY r.createdAt ASC
            """)
    List<Resale> findYardResalesForStatistics(
            @Param("yardId") Long yardId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<Resale> findByMaterialScrapYardYardIdAndCreatedAtBetween(
            Long yardId,
            LocalDateTime start,
            LocalDateTime end
    );
}