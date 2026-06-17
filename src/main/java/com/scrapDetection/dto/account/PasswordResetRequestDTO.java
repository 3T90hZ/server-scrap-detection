package com.scrapDetection.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDTO {

    private String phoneNumbers;   // Use either phone or email

    private String email;

    // At least one must be provided (validated in service)
}