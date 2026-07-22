package com.scrapDetection.service;

import com.scrapDetection.dto.scrapyard.ScrapYardRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardResponseDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardStatusRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardUpdateRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
public interface ScrapYardService {

    // Create a new ScrapYard
    ScrapYardResponseDTO createScrapYardRequest(ScrapYardRequestDTO requestDTO);

    // Update an existing ScrapYard
    ScrapYardResponseDTO updateScrapYard(Long yardId, ScrapYardUpdateRequestDTO requestDTO);

    // Find ScrapYard by ID
    ScrapYardResponseDTO getScrapYardById(Long yardId);

    // Find ScrapYard by name
    ScrapYardResponseDTO getScrapYardByName(String yardName);     // ← NEW

    // Get all ScrapYards
    List<ScrapYardResponseDTO> getAllScrapYards();

    // Get all ScrapYards with pagination
    Page<ScrapYardResponseDTO> getAllScrapYards(Pageable pageable);

    // Find ScrapYards by status
    List<ScrapYardResponseDTO> getScrapYardsByStatus(String status);

    // Find ScrapYard by name
    List<ScrapYardResponseDTO> searchScrapYardsByName(String yardName); // ← NEW

    ScrapYardResponseDTO updateScrapYardStatus(ScrapYardStatusRequestDTO requestDTO);

    // Delete ScrapYard by ID
    void deleteScrapYard(Long yardId);
}
