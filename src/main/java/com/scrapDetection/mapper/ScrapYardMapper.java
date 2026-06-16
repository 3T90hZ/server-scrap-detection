package com.scrapDetection.mapper;

import com.scrapDetection.dto.scrapyard.ScrapYardRequestDTO;
import com.scrapDetection.dto.scrapyard.ScrapYardResponseDTO;
import com.scrapDetection.entity.ScrapYard;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScrapYardMapper {

    public ScrapYard toEntity(ScrapYardRequestDTO dto) {
        if (dto == null) return null;

        ScrapYard scrapYard = new ScrapYard();
        scrapYard.setAddress(dto.getAddress());
        scrapYard.setPhoneNumbers(dto.getPhoneNumbers());
        scrapYard.setStatus(dto.getStatus());
        return scrapYard;
    }

    public ScrapYardResponseDTO toResponseDTO(ScrapYard entity) {
        if (entity == null) return null;

        return ScrapYardResponseDTO.builder()
                .yardId(entity.getYardId())
                .address(entity.getAddress())
                .phoneNumbers(entity.getPhoneNumbers())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<ScrapYardResponseDTO> toResponseDTOList(List<ScrapYard> entities) {
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // For partial updates
    public void updateEntityFromDTO(ScrapYardRequestDTO dto, ScrapYard entity) {
        if (dto.getAddress() != null) {
            entity.setAddress(dto.getAddress());
        }
        if (dto.getPhoneNumbers() != null) {
            entity.setPhoneNumbers(dto.getPhoneNumbers());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }
}
