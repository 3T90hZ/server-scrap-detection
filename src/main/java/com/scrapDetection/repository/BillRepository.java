package com.scrapDetection.repository;

import com.scrapDetection.entity.Bill;
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
}