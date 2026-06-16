package com.scrapDetection.dto.scrapyard;

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
public class ScrapYardRequestDTO {

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    @Size(max = 255, message = "Phone numbers cannot exceed 255 characters")
    private String phoneNumbers;

    @NotBlank(message = "Status is required")
    private String status;
}
