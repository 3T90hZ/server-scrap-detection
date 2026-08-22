package com.scrapDetection.dto.bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemResponseDTO {

    private Long transactionId;
    private Long materialId;
    private String itemName;
    private Double weight;
    private Double lineWorth;
}