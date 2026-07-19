package com.scrapDetection.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeAccountStatusRequestDTO {
    @NotNull(message = "Account ID is required")
    Long accountId;

    @NotBlank(message = "Status is required")
    String status;
}
