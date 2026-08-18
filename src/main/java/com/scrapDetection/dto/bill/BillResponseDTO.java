package com.scrapDetection.dto.bill;

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

    private Long billId;
    private Long customerId;
    private String customerName;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private Double totalWorth;
    private Long yardId;
    private String yardName;
    private List<BillItemResponseDTO> items;
}