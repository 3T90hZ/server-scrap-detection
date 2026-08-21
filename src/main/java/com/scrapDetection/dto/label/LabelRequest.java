package com.scrapDetection.dto.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelRequest {
    @NotBlank(message = "Label name is required")
    private String labelName;

    @NotNull(message = "Material ID is required")
    private Long materialId;
}
