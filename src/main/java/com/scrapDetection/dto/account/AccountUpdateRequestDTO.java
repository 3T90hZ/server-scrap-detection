package com.scrapDetection.dto.account;

import jakarta.validation.constraints.Email;
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
public class AccountUpdateRequestDTO {


    @Size(min = 4, max = 50)
    private String accountName;

    @Pattern(regexp = "^[0-9\\s]*$", message = "Invalid phone number")
    private String phoneNumbers;

    @Email
    private String email;

    @Size(min= 6)
    private String password;
    private String status;   // ACTIVE, INACTIVE, etc.
}