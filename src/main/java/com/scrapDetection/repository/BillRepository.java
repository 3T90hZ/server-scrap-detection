package com.scrapDetection.repository;

import com.scrapDetection.entity.Bill;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByCustomerAccountId(Long customerId);

    @Query("SELECT b FROM Bill b WHERE b.createdBy.accountId = :staffId")
    List<Bill> findByCreatedByAccountId(@Param("staffId") Long staffId);

    List<Bill> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT b FROM Bill b JOIN b.transactions t WHERE t.material.scrapYard.yardId = :yardId")
    List<Bill> findByYardId(@Param("yardId") Long yardId);

    @EntityGraph(attributePaths = {"transactions", "transactions.material"})
    @Query("""
            SELECT b FROM Bill b
            WHERE b.createdBy.accountId = :staffId
              AND b.createdAt >= :startOfDay
              AND b.createdAt < :nextDay
            ORDER BY b.createdAt ASC
            """)
    List<Bill> findStaffBillsForStatistics(
            @Param("staffId") Long staffId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("nextDay") LocalDateTime nextDay
    );

    /**
     * Owner profit – all bills that contain materials belonging to the yard,
     * optionally filtered by date range.
     */
    @EntityGraph(attributePaths = {"transactions", "transactions.material"})
    @Query("""
            SELECT DISTINCT b FROM Bill b
            JOIN b.transactions t
            WHERE t.material.scrapYard.yardId = :yardId
              AND (:startDate IS NULL OR b.createdAt >= :startDate)
              AND (:endDate IS NULL OR b.createdAt <= :endDate)
            ORDER BY b.createdAt ASC
            """)
    List<Bill> findYardBillsForStatistics(
            @Param("yardId") Long yardId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}