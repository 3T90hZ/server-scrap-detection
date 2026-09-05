package com.scrapDetection.dto.bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillSummaryDTO {

    private Long billId;
    private String customerName;
    private String createdByName;
    private Double totalWorth;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private Boolean hasOverridden;
}