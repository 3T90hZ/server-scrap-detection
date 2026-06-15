package com.scrapDetection.repository;

import com.scrapDetection.entity.ScrapYard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapYardRepository extends JpaRepository<ScrapYard, Long> {

    List<ScrapYard> findByStatus(String status);

    boolean existsByAddress(String address);
}
