package com.scrapDetection.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryDTO {

    private Long transactionId;
    private String itemName;
    private Double weight;
    private Double totalWorth;
    private String customerName;
    private String createdBy;
    private LocalDateTime createdAt;
}