package com.scrapDetection.dto.account;

import com.scrapDetection.entity.AccountStatus;
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

    @NotNull(message = "Status is required")
    AccountStatus status;
}
