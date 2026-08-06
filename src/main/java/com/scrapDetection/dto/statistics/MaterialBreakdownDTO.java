package com.scrapDetection.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialBreakdownDTO {
    private String materialName;
    private Double totalWeight;
    private Double totalSpent;
}
