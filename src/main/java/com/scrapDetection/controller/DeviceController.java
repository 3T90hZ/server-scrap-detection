package com.scrapDetection.controller;

import com.scrapDetection.dto.device.DeviceRequestDTO;
import com.scrapDetection.dto.device.DeviceResponseDTO;
import com.scrapDetection.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;
    // Create new device
    @PreAuthorize("hasRole('YARD_OWNER')")
    @PostMapping
    public ResponseEntity<DeviceResponseDTO> createDevice(
            @Valid @RequestBody DeviceRequestDTO requestDTO) {

        DeviceResponseDTO response = deviceService.createDevice(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Update device
    @PreAuthorize("hasRole('YARD_OWNER')")
    @PutMapping("/{deviceId}")
    public ResponseEntity<DeviceResponseDTO> updateDevice(
            @PathVariable Long deviceId,
            @Valid @RequestBody DeviceRequestDTO requestDTO) {

        DeviceResponseDTO response = deviceService.updateDevice(deviceId, requestDTO);
        return ResponseEntity.ok(response);
    }

    // Get device by id
    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceResponseDTO> getDeviceById(@PathVariable Long deviceId) {
        DeviceResponseDTO response = deviceService.getDeviceById(deviceId);
        return ResponseEntity.ok(response);
    }

    // Get all devices by in yard
    @PreAuthorize("hasRole('YARD_OWNER')")
    @GetMapping("/my-yard")
    public ResponseEntity<List<DeviceResponseDTO>> getMyYardDevices() {
        List<DeviceResponseDTO> response = deviceService.getMyYardDevices();
        return ResponseEntity.ok(response);
    }

    // Get all device by yardID
    @PreAuthorize("hasAnyRole('ADMIN', 'YARD_OWNER')")
    @GetMapping("/yard/{yardId}")
    public ResponseEntity<List<DeviceResponseDTO>> getDevicesByYard(@PathVariable Long yardId) {
        List<DeviceResponseDTO> response = deviceService.getDevicesByYardId(yardId);
        return ResponseEntity.ok(response);
    }

    // Get all device (Pagination)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<DeviceResponseDTO>> getAllDevices(Pageable pageable) {
        Page<DeviceResponseDTO> response = deviceService.getAllDevices(pageable);
        return ResponseEntity.ok(response);
    }

    // Delete device
    @PreAuthorize("hasRole('YARD_OWNER')")
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }
}
