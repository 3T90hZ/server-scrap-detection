package com.scrapDetection.dto.device;

import com.scrapDetection.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeviceViewerAccessDTO {
    private Long accountId;
    private Role role;
    private Long deviceId;
}
