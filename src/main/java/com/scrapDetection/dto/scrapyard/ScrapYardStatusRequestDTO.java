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
    @NotNull(message = "Yard ID is required")
    Long yardId;

    @NotBlank(message = "Status is required")
    String status;
}
