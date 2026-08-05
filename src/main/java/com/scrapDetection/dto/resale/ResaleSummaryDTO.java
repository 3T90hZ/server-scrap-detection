package com.scrapDetection.dto.resale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResaleSummaryDTO {

    private Long resaleId;
    private String itemName;
    private Double weight;
    private Double unitPrice;
    private Double totalWorth;
    private String factoryName;
    private String createdBy;
    private LocalDateTime createdAt;
}