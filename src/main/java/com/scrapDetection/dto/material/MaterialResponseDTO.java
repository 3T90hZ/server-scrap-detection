package com.scrapDetection.dto.material;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialResponseDTO {

    private Long materialId;
    private Long yardId;
    private String icon;
    private String itemName;
    private Double itemPrice;
    private String unit;
    private String status;
    private LocalDateTime updatedAt;
}