package com.scrapDetection.dto.account;

import com.scrapDetection.entity.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoResponseDTO {
    private Long accountId;
    private String accountName;
    private String phoneNumbers;
    private AccountStatus status;
}
