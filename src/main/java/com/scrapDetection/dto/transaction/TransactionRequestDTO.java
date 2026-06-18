package com.scrapDetection.dto.transaction;

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
public class TransactionRequestDTO {

    @NotNull(message = "Material ID is required")
    private Long materialId;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;

    // ownerOrStaff will be set automatically from current logged-in user (staff/yard owner)
}