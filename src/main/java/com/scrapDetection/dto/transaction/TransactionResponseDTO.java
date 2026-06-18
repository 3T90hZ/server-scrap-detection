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
public class TransactionResponseDTO {

    private Long transactionId;
    private Long materialId;
    private String itemName;
    private Double weight;
    private Double totalWorth;
    private Long customerId;
    private String customerName;
    private Long ownerOrStaffId;
    private String staffName;
    private LocalDateTime createdAt;
    private Long yardId;
    private String yardName;
}