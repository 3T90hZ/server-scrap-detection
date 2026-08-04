package com.scrapDetection.dto.material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequestDTO {

    @NotBlank(message = "Item name is required")
    @Size(max = 200, message = "Item name cannot exceed 200 characters")
    private String itemName;

    @NotNull(message = "Item price is required")
    @Positive(message = "Price must be positive")
    private Double itemPrice;

    @NotBlank(message = "Unit is required")
    private String unit;

    private String status; // ACTIVE/INACTIVE

    @NotBlank
    private String icon;

}