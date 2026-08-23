package com.scrapDetection.dto.scrapyard;

import com.scrapDetection.entity.YardStatus;
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

    @NotNull(message = "Status is required")
    private YardStatus status;
}
