package com.scrapDetection.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponseDTO {
    private String billId;
    private String transactionType;
    private List<BillItemResponseDTO> items;
    private Double totalWeight;
    private Double totalWorth;
    private String customerName;
    private String createdBy;
    private LocalDateTime createdAt;
}
