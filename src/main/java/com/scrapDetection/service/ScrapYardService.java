package com.scrapDetection.service;

import com.scrapDetection.dto.scrapyard.ScrapYardRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardResponseDTO;
import com.scrapDetection.entity.ScrapYard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ScrapYardService {

    // Create a new ScrapYard
    ScrapYardResponseDTO createScrapYard(ScrapYardRequestDTO requestDTO);

    // Update an existing ScrapYard
    ScrapYardResponseDTO updateScrapYard(Long yardId, ScrapYardRequestDTO requestDTO);

    // Find ScrapYard by ID
    ScrapYardResponseDTO getScrapYardById(Long yardId);

    // Get all ScrapYards
    List<ScrapYardResponseDTO> getAllScrapYards();

    // Get all ScrapYards with pagination
    Page<ScrapYardResponseDTO> getAllScrapYards(Pageable pageable);

    // Find ScrapYards by status
    List<ScrapYardResponseDTO> getScrapYardsByStatus(String status);

    // Delete ScrapYard by ID
    void deleteScrapYard(Long yardId);

    // Check if ScrapYard exists by ID
    boolean existsById(Long yardId);
}
