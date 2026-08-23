package com.scrapDetection.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddStaffRequestDTO {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}