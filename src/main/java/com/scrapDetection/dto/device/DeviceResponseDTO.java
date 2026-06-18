package com.scrapDetection.dto.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponseDTO {

    private Long deviceId;
    private Long yardId;
    private String yardName;           // For better UX
    private String deviceName;
    private String ipAddress;
}