package com.scrapDetection.mapper;

import com.scrapDetection.dto.detection.DetectionRequestDTO;
import com.scrapDetection.dto.detection.DetectionResponseDTO;
import com.scrapDetection.entity.Material;
import com.scrapDetection.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class DetectionMapper {
    public Transaction toEntity(DetectionRequestDTO dto, Material material) {

        if (dto == null || material == null) {
            return null;
        }

        return Transaction.builder()
                .material(material)
                .weight(dto.getWeightG())
                .customer(null) // v1: nullable
                .ownerOrStaff(null) // v1: nullable
                .build();
    }

    public DetectionResponseDTO toSuccessResponse(
            Transaction transaction,
            Material material) {

        return DetectionResponseDTO.success(
                transaction.getTransactionId(),
                material.getItemName(),
                transaction.getWeight(),
                material.getItemPrice()
        );
    }

    public DetectionResponseDTO toErrorResponse(String message) {
        return DetectionResponseDTO.error(message);
    }
}
