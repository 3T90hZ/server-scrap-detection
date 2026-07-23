package com.scrapDetection.service;

import com.scrapDetection.dto.material.MaterialRequestDTO;
import com.scrapDetection.dto.material.MaterialResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MaterialService {

    // Yard Owner: Create new material
    MaterialResponseDTO createMaterial(MaterialRequestDTO requestDTO);

    // Yard Owner: Update existing material
    MaterialResponseDTO updateMaterial(Long materialId, MaterialRequestDTO requestDTO);

    // Get material by ID (for both yard owner and customers)
    MaterialResponseDTO getMaterialById(Long materialId);

    List<MaterialResponseDTO> getMaterialsByYardId(Long yardId);

    // Get all materials in a specific yard
    List<MaterialResponseDTO> getMaterialsByYardIdAndStatus(Long yardId, String status);

    // Get all materials with pagination
    Page<MaterialResponseDTO> getAllActiveMaterials(Pageable pageable);

    // Search materials by item name (for customers)
    List<MaterialResponseDTO> searchActiveMaterialsByName(String itemName);

    // Get materials by status
    List<MaterialResponseDTO> getMaterialsByStatus(String status);

    // Yard Owner: Delete material
    void deleteMaterial(Long id);

    List<MaterialResponseDTO> searchMaterialsInYardByName(Long yardId, String keyword);
}