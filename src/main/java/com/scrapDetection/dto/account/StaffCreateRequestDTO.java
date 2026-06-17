package com.scrapDetection.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffCreateRequestDTO {

    @NotBlank(message = "Display name is required")
    @Size(min = 3, max = 50)
    private String accountName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9\\s]*$", message = "Invalid phone number")
    private String phoneNumbers;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 120)
    private String password;

    private String email;
}