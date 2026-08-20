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
public class AccountUpdateRequestDTO {


    @Size(min = 4, max = 50)
    private String accountName;

    @Pattern(regexp = "^[0-9\\s]*$", message = "Invalid phone number")
    @Min(value = 10, message = "Invalid phone number")
    @Max(value = 12, message = "Invalid phone number")
    private String phoneNumbers;

    @Email
    private String email;

    @Size(min= 6)
    private String password;
}