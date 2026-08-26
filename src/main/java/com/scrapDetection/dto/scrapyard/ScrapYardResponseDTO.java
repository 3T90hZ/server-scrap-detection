package com.scrapDetection.dto.scrapyard;

import com.scrapDetection.entity.YardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapYardResponseDTO {

    private Long yardId;
    private String yardName;
    private String address;
    private String phoneNumbers;
    private String openHour;
    private String closeHour;
    private Double latitude;
    private Double longitude;
    private YardStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
