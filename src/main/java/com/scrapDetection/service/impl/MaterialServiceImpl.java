package com.scrapDetection.service.impl;

import com.scrapDetection.dto.material.MaterialRequestDTO;
import com.scrapDetection.dto.material.MaterialResponseDTO;
import com.scrapDetection.entity.Material;
import com.scrapDetection.entity.Transaction;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.MaterialMapper;
import com.scrapDetection.repository.MaterialRepository;
import com.scrapDetection.repository.TransactionRepository;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    @Override
    public MaterialResponseDTO createMaterial(MaterialRequestDTO requestDTO) {
        Material material = materialMapper.toEntity(requestDTO);

        // Get current logged-in user (Yard Owner)
        var currentUser = accountService.getCurrentUser();

        if (currentUser.getScrapYard() == null) {
            throw new InvalidRequestException("You must be assigned to a scrap yard to manage materials");
        }

        // Assign material to current user's yard
        material.setScrapYard(currentUser.getScrapYard());
        material.setStatus("ACTIVE");


        Material savedMaterial = materialRepository.save(material);
        return materialMapper.toResponseDTO(savedMaterial);
    }

    @Override
    public MaterialResponseDTO updateMaterial(Long materialId, MaterialRequestDTO requestDTO) {
        Material existingMaterial = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));

        // Yard Ownership Check
        validateYardOwnership(existingMaterial);

        materialMapper.updateEntityFromDTO(requestDTO, existingMaterial);
        existingMaterial.setUpdatedAt(LocalDateTime.now());
        Material updatedMaterial = materialRepository.save(existingMaterial);
        return materialMapper.toResponseDTO(updatedMaterial);
    }

    @Override
    public MaterialResponseDTO getMaterialById(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));

        return materialMapper.toResponseDTO(material);
    }

    @Override
    public List<MaterialResponseDTO> getMaterialsByYardId(Long yardId) {
        List<Material> materials = materialRepository.findByScrapYardYardId(yardId);
        return materialMapper.toResponseDTOList(materials);
    }

    @Override
    public List<MaterialResponseDTO> getMaterialsByYardIdAndStatus(Long yardId, String status) {
        List<Material> materials = materialRepository.findByScrapYardYardIdAndStatus(yardId, status);
        return materialMapper.toResponseDTOList(materials);
    }

    @Override
    public Page<MaterialResponseDTO> getAllActiveMaterials(Pageable pageable) {
        Page<Material> materialPage = materialRepository.findAll(pageable);
        return materialPage.map(materialMapper::toResponseDTO);
    }

    @Override
    public List<MaterialResponseDTO> searchActiveMaterialsByName(String itemName) {
        List<Material> materials = materialRepository.findByItemNameContainingIgnoreCaseAndStatus(itemName, "ACTIVE");
        return materialMapper.toResponseDTOList(materials);
    }

    @Override
    public List<MaterialResponseDTO> searchMaterialsInYardByName(Long yardId, String keyword){
        List<Material> materials = materialRepository.findByScrapYardYardIdAndItemNameContainingIgnoreCase(yardId, keyword);
        return materialMapper.toResponseDTOList(materials);
    }

    @Override
    public List<MaterialResponseDTO> getMaterialsByStatus(String status) {
        List<Material> materials = materialRepository.findByStatus(status);
        return materialMapper.toResponseDTOList(materials);
    }

    @Override
    public void deleteMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", id));

        // Yard Ownership Check
        validateYardOwnership(material);

        List<Transaction> transactions = transactionRepository.findByMaterialMaterialId(id);

        if (transactions.isEmpty()) {
            materialRepository.deleteById(id);
        }
        else{
            material.setUpdatedAt(LocalDateTime.now());
            material.setStatus("INACTIVE");
            materialRepository.save(material);
        }
    }

    // ==================== Private Helper ====================

    /**
     * Validates that the current user can only manage materials in their own yard
     */
    private void validateYardOwnership(Material material) {
        var currentUser = accountService.getCurrentUser();

        if (currentUser.getScrapYard() == null) {
            throw new InvalidRequestException("You are not assigned to any scrap yard");
        }

        if (material.getScrapYard() == null ||
                !material.getScrapYard().getYardId().equals(currentUser.getScrapYard().getYardId())) {

            throw new InvalidRequestException("You can only manage materials in your own scrap yard");
        }
    }
}