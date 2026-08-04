package com.scrapDetection.dto.device;

import com.scrapDetection.entity.DeviceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private DeviceStatus status;
}
