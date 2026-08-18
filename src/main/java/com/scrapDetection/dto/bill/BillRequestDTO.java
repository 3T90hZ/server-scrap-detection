package com.scrapDetection.dto.bill;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillRequestDTO {

    private Long customerId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<BillItemRequestDTO> items;
}