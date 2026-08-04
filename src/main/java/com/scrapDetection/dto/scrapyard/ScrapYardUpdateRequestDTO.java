package com.scrapDetection.dto.scrapyard;

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
public class ScrapYardUpdateRequestDTO {
    @Size(min = 4, max = 50)
    private String yardName;

    @Pattern(regexp = "^[0-9\\s]*$", message = "Invalid phone number")
    private String phoneNumbers;

    private String address;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Giờ phải đúng định dạng HH:mm")
    private String openHour;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Giờ phải đúng định dạng HH:mm")
    private String closeHour;
}
