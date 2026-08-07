package com.scrapDetection.dto.transaction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @Pattern(regexp = "buy|sell", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Transaction type must be buy or sell")
    private String transactionType;

    @Valid
    @NotEmpty(message = "A bill must contain at least one item")
    @Size(max = 100, message = "A bill cannot contain more than 100 items")
    private List<BillItemRequestDTO> items;
}
