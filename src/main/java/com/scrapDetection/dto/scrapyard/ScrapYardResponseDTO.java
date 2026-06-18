package com.scrapDetection.dto.scrapyard;

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
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
