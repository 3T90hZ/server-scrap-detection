package com.scrapDetection.mapper;

import com.scrapDetection.dto.material.MaterialRequestDTO;
import com.scrapDetection.dto.material.MaterialResponseDTO;
import com.scrapDetection.entity.Material;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MaterialMapper {

    public Material toEntity(MaterialRequestDTO dto) {
        if (dto == null) return null;

        Material material = new Material();
        material.setItemName(dto.getItemName());
        material.setItemPrice(dto.getItemPrice());
        material.setStatus(dto.getStatus());
        return material;
    }

    public MaterialResponseDTO toResponseDTO(Material entity) {
        if (entity == null) return null;

        return MaterialResponseDTO.builder()
                .materialId(entity.getMaterialId())
                .yardId(entity.getScrapYard() != null ? entity.getScrapYard().getYardId() : null)
                .itemName(entity.getItemName())
                .itemPrice(entity.getItemPrice())
                .status(entity.getStatus())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<MaterialResponseDTO> toResponseDTOList(List<Material> materials) {
        return materials.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(MaterialRequestDTO dto, Material entity) {
        if (dto.getItemName() != null) {
            entity.setItemName(dto.getItemName());
        }
        if (dto.getItemPrice() != null) {
            entity.setItemPrice(dto.getItemPrice());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }
}