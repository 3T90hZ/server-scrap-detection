package com.scrapDetection.dto.account;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPhoneNumberRequestDTO {
    @Pattern(regexp = "^[0-9\\s]*$", message = "Invalid phone number format")
    String phoneNumber;
}
