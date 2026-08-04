package com.scrapDetection.dto.device;

import com.scrapDetection.entity.DeviceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private DeviceStatus status;
    private String apiKeyPrefix;       // identification only — never the full key
    private String firmwareVersion;
    private String desiredFirmwareVersion;
    private LocalDateTime lastSeenAt;

    // Raw API key. ONLY ever populated by createDevice() and regenerateApiKey()
    // in DeviceServiceImpl, right after a new key is generated — every other
    // response (get/list/update/status) leaves this null, since the raw key
    // is never stored and can't be recovered afterward.
    private String apiKey;
}