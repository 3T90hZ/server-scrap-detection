package com.scrapDetection.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerProfitResponseDTO {
    private Double totalSpent;
    private Double estimatedRevenue;
    private Double totalProfit;
}
