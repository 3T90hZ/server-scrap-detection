package com.scrapDetection.service;

import com.scrapDetection.dto.device.DeviceRequestDTO;
import com.scrapDetection.dto.device.DeviceResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeviceService {

    // Create new device
    DeviceResponseDTO createDevice(DeviceRequestDTO requestDTO);

    // Update device
    DeviceResponseDTO updateDevice(Long deviceId, DeviceRequestDTO requestDTO);

    // Get device by ID
    DeviceResponseDTO getDeviceById(Long deviceId);

    // Get device by YardID
    List<DeviceResponseDTO> getMyYardDevices();

    List<DeviceResponseDTO> getDevicesByYardId(Long yardId);

    // Get all device
    Page<DeviceResponseDTO> getAllDevices(Pageable pageable);

    // Delete device
    void deleteDevice(Long deviceId);

    // Check if device exists
    boolean existsById(Long deviceId);
}
