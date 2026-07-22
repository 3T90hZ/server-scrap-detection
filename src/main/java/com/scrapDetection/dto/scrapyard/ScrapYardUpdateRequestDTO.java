package com.scrapDetection.dto.scrapyard;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapYardUpdateRequestDTO {
    @Size(min = 4, max = 50)
    private String yardName;

    @Pattern(regexp = "^[0-9\\s]*$", message = "Invalid phone number")
    private String phoneNumbers;

    private String address;

    private LocalDateTime openHour;

    private LocalDateTime closeHour;
}
