package com.scrapDetection.repository;

import com.scrapDetection.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByMaterialMaterialId(Long materialId);

    List<Transaction> findByMaterialScrapYardYardId(Long yardId);
}
