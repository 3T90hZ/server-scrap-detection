package com.scrapDetection.dto.scrapyard;

import com.scrapDetection.entity.YardStatus;
import jakarta.validation.constraints.NotBlank;
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
    private YardStatus status;
}
