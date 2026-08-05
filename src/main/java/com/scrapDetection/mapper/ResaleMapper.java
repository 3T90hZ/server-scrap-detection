package com.scrapDetection.mapper;

import com.scrapDetection.dto.resale.ResaleRequestDTO;
import com.scrapDetection.dto.resale.ResaleResponseDTO;
import com.scrapDetection.dto.resale.ResaleSummaryDTO;
import com.scrapDetection.entity.Resale;
import com.scrapDetection.entity.ResaleTotal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResaleMapper {

    public Resale toEntity(ResaleRequestDTO dto) {
        if (dto == null) return null;

        Resale resale = new Resale();
        resale.setWeight(dto.getWeight());
        resale.setFactoryName(dto.getFactoryName());
        resale.setUnitPrice(dto.getUnitPrice());   // ← new
        return resale;
    }

    public ResaleResponseDTO toResponseDTO(Resale entity) {
        if (entity == null) return null;

        ResaleTotal total = entity.getResaleTotal();

        return ResaleResponseDTO.builder()
                .resaleId(entity.getResaleId())
                .materialId(entity.getMaterial().getMaterialId())
                .itemName(entity.getMaterial().getItemName())
                .weight(entity.getWeight())
                .unitPrice(entity.getUnitPrice())
                .totalWorth(total != null ? total.getTotalWorth() : null)
                .factoryName(entity.getFactoryName())
                .ownerOrStaffId(entity.getCreatedBy() != null ? entity.getCreatedBy().getAccountId() : null)
                .staffName(entity.getCreatedBy() != null ? entity.getCreatedBy().getAccountName() : null)
                .yardId(entity.getMaterial().getScrapYard().getYardId())
                .yardName(entity.getMaterial().getScrapYard().getYardName())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ResaleSummaryDTO toSummaryDTO(Resale entity) {
        if (entity == null) return null;

        return ResaleSummaryDTO.builder()
                .resaleId(entity.getResaleId())
                .itemName(entity.getMaterial().getItemName())
                .weight(entity.getWeight())
                .unitPrice(entity.getUnitPrice())                 // ← new
                .totalWorth(entity.getResaleTotal() != null ? entity.getResaleTotal().getTotalWorth() : null)
                .factoryName(entity.getFactoryName())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getAccountName() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<ResaleResponseDTO> toResponseDTOList(List<Resale> resales) {
        return resales.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ResaleSummaryDTO> toSummaryDTOList(List<Resale> resales) {
        return resales.stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }
}