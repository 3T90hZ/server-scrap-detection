package com.scrapDetection.repository;

import com.scrapDetection.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCustomerAccountId(Long customerId);

    @Query("SELECT t FROM Transaction t WHERE t.ownerOrStaff.accountId = :staffId")
    List<Transaction> findByOwnerOrStaffAccountId(@Param("staffId") Long staffId);

    List<Transaction> findByMaterialMaterialId(Long materialId);

    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findByMaterialScrapYardYardId(Long yardId);

    List<Transaction> findByMaterialScrapYardYardIdOrderByCreatedAtDesc(Long yardId);
}
