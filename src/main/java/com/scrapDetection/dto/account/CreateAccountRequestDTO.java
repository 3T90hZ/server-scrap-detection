package com.scrapDetection.dto.account;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequestDTO {

    @NotBlank(message = "Display name (account name) is required")
    @Size(min = 3, max = 50, message = "Display name must be between 3 and 50 characters")
    private String accountName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9\\s]*$", message = "Invalid phone number format")
    @Min(value = 10, message = "Invalid phone number")
    @Max(value = 12, message = "Invalid phone number")
    private String phoneNumbers;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 120, message = "Password must be between 8 and 120 characters")
    private String password;

    @Email(message = "Invalid email format")
    private String email;
}