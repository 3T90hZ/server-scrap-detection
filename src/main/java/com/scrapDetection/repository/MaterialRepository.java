package com.scrapDetection.repository;

import com.scrapDetection.entity.Material;
import com.scrapDetection.entity.MaterialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByScrapYardYardIdAndStatus(Long yardId, MaterialStatus status);

    List<Material> findByStatus(String status);

    List<Material> findByItemNameContainingIgnoreCaseAndStatus(String keyword, MaterialStatus status);

    boolean existsByScrapYardYardIdAndItemNameIgnoreCase(
            Long yardId,
            String itemName
    );

    List<Material> findByScrapYardYardIdAndItemNameContainingIgnoreCase(
            Long yardId,
            String keyword
    );

    List<Material> findByScrapYardYardId(Long yardId);
}
