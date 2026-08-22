package com.scrapDetection.dto.resale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResaleResponseDTO {

    private Long resaleId;
    private Long materialId;
    private String itemName;
    private Double weight;
    private Double unitPrice;
    private Double totalWorth;
    private String factoryName;
    private Long ownerOrStaffId;
    private String staffName;
    private LocalDateTime createdAt;
    private Long yardId;
    private String yardName;
}