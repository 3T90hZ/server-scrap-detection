package com.scrapDetection.repository;

import com.scrapDetection.entity.TransactionTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionTotalRepository extends JpaRepository<TransactionTotal, Long> {

    Optional<TransactionTotal> findByTransactionTransactionId(Long transactionId);
}