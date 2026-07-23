package com.scrapDetection.mapper;

import com.scrapDetection.dto.device.DeviceRequestDTO;
import com.scrapDetection.dto.device.DeviceResponseDTO;
import com.scrapDetection.entity.Device;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeviceMapper {

    public Device toEntity(DeviceRequestDTO dto) {
        if (dto == null) return null;

        Device device = new Device();
        device.setDeviceName(dto.getDeviceName());
        device.setIpAddress(dto.getIpAddress());
        return device;
    }

    public DeviceResponseDTO toResponseDTO(Device entity) {
        if (entity == null) return null;

        return DeviceResponseDTO.builder()
                .deviceId(entity.getDeviceId())
                .yardId(entity.getScrapYard() != null ? entity.getScrapYard().getYardId() : null)
                .yardName(entity.getScrapYard() != null ? entity.getScrapYard().getYardName() : null)
                .deviceName(entity.getDeviceName())
                .ipAddress(entity.getIpAddress())
                .status(entity.getStatus())
                .apiKeyPrefix(entity.getApiKeyPrefix())
                .firmwareVersion(entity.getFirmwareVersion())
                .desiredFirmwareVersion(entity.getDesiredFirmwareVersion())
                .lastSeenAt(entity.getLastSeenAt())
                // apiKey intentionally omitted — only set by
                // DeviceServiceImpl.createDevice()/regenerateApiKey()
                .build();
    }

    public List<DeviceResponseDTO> toResponseDTOList(List<Device> devices) {
        return devices.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(DeviceRequestDTO dto, Device entity) {
        if (dto.getDeviceName() != null) {
            entity.setDeviceName(dto.getDeviceName());
        }
        if (dto.getIpAddress() != null) {
            entity.setIpAddress(dto.getIpAddress());
        }
    }
}