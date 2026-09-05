package com.scrapDetection.dto.bill;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemRequestDTO {

    @NotNull(message = "Material ID is required")
    private Long materialId;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;

    private Boolean isOverridden;
    private Long originalMaterialId;
    private Double originalWeight;
}