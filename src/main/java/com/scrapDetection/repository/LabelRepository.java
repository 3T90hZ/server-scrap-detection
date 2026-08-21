package com.scrapDetection.repository;

import com.scrapDetection.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    List<Label> findByScrapYardYardId(Long yardId);

    Optional<Label> findByScrapYardYardIdAndMaterialMaterialId(
            Long yardId,
            Long materialId
    );

    Optional<Label> findByScrapYardYardIdAndLabel(
            Long yardId,
            String label
    );

}