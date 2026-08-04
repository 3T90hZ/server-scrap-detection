package com.scrapDetection.repository;

import com.scrapDetection.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByScrapYardYardIdAndStatus(Long yardId, String status);

    List<Material> findByStatus(String status);

    List<Material> findByItemNameContainingIgnoreCaseAndStatus(String keyword, String status);

    Optional<Material> findFirstByItemNameIgnoreCase(String itemName);

    List<Material> findByScrapYardYardIdAndItemNameContainingIgnoreCase(
            Long yardId,
            String keyword
    );

    List<Material> findByScrapYardYardId(Long yardId);
}
