package com.scrapDetection.dto.scrapyard;

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
public class ScrapYardStatusRequestDTO {

    @NotBlank(message = "Status is required")
    private String status;
}
