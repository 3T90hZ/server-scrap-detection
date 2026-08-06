package com.scrapDetection.repository;

import com.scrapDetection.entity.Transaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCustomerAccountId(Long customerId);

    @Query("SELECT t FROM Transaction t WHERE t.createdBy.accountId = :staffId")
    List<Transaction> findByCreated_byAccountId(@Param("staffId") Long staffId);

    List<Transaction> findByMaterialMaterialId(Long materialId);

    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findByMaterialScrapYardYardId(Long yardId);

    @EntityGraph(attributePaths = {"material", "transactionTotal"})
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.createdBy.accountId = :staffId
              AND t.createdAt >= :startOfDay
              AND t.createdAt < :nextDay
            ORDER BY t.createdAt ASC
            """)
    List<Transaction> findStaffTransactionsForStatistics(
            @Param("staffId") Long staffId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("nextDay") LocalDateTime nextDay
    );

    @EntityGraph(attributePaths = {"material", "transactionTotal"})
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.material.scrapYard.yardId = :yardId
              AND (:startDate IS NULL OR t.createdAt >= :startDate)
              AND (:endDate IS NULL OR t.createdAt <= :endDate)
            ORDER BY t.createdAt ASC
            """)
    List<Transaction> findYardTransactionsForStatistics(
            @Param("yardId") Long yardId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
