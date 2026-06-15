package com.scrapDetection.repository;

import com.scrapDetection.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCustomerAccountId(Long customerId);

    List<Transaction> findByOwnerOrStaffAccountId(Long staffId);

    List<Transaction> findByMaterialMaterialId(Long materialId);

    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findByScrapYardYardId(Long yardId); // You may need a custom query if not directly mapped
}
