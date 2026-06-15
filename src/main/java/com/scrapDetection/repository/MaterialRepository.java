package com.scrapDetection.repository;

import com.scrapDetection.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByScrapYardYardId(Long yardId);

    List<Material> findByStatus(String status);

    List<Material> findByItemNameContainingIgnoreCase(String itemName);
}
