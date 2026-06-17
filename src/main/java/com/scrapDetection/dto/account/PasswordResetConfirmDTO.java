package com.scrapDetection.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetConfirmDTO {

    @NotBlank(message = "Reset token/code is required")
    private String resetToken;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 120)
    private String newPassword;
}