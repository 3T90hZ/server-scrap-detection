package com.scrapDetection.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffSummaryResponseDTO {
    private Double todayTotalSpent;
    private Double todayTotalWeight;
    private Integer todayTransactionsCount;
    private List<MaterialBreakdownDTO> materialBreakdown;
}
