package com.scrapDetection.dto.scrapyard;

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
public class ScrapYardRequestDTO {

    @NotBlank(message = "Yard name is required")
    @Size(min = 3, max = 150, message = "Yard name must be between 3 and 150 characters")
    private String yardName;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    @NotBlank(message = "Phone numbers are required")
    @Size(max = 20, message = "Phone numbers cannot exceed 10 characters")
    @Pattern(regexp = "^[0-9\\s]*$", message = "Phone numbers should contain only digits and spaces")
    private String phoneNumbers;

    @NotBlank(message = "Status is required")
    private String status;
}
