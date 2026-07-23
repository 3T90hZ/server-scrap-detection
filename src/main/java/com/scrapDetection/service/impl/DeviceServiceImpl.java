package com.scrapDetection.service.impl;

import com.scrapDetection.dto.device.DeviceRequestDTO;
import com.scrapDetection.dto.device.DeviceResponseDTO;
import com.scrapDetection.entity.Device;
import com.scrapDetection.entity.DeviceStatus;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.DeviceMapper;
import com.scrapDetection.repository.DeviceRepository;
import com.scrapDetection.security.device.DeviceApiKeyService;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;
    private final AccountService accountService;
    private final DeviceApiKeyService deviceApiKeyService;

    @Override
    public DeviceResponseDTO createDevice(DeviceRequestDTO requestDTO) {
        Device device = deviceMapper.toEntity(requestDTO);

        // Get current Yard Owner and assign to their yard
        var currentUser = accountService.getCurrentUser();

        if (currentUser.getScrapYard() == null) {
            throw new InvalidRequestException("You must be assigned to a scrap yard to manage devices");
        }

        device.setScrapYard(currentUser.getScrapYard());

        // Generate a fresh API key. The raw value is only ever known here,
        // in memory, for this one request — only its hash gets persisted.
        String rawKey = deviceApiKeyService.generateRawKey();
        device.setApiKeyHash(deviceApiKeyService.hash(rawKey));
        device.setApiKeyPrefix(deviceApiKeyService.extractPrefix(rawKey));
        device.setApiKeyRotatedAt(LocalDateTime.now());
        device.setStatus(DeviceStatus.ACTIVE);

        Device savedDevice = deviceRepository.save(device);

        DeviceResponseDTO response = deviceMapper.toResponseDTO(savedDevice);
        response.setApiKey(rawKey);   // shown once — caller must save it now
        return response;
    }

    @Override
    public DeviceResponseDTO updateDevice(Long deviceId, DeviceRequestDTO requestDTO) {
        Device existingDevice = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        // Ownership validation
        validateYardOwnership(existingDevice);

        deviceMapper.updateEntityFromDTO(requestDTO, existingDevice);

        Device updatedDevice = deviceRepository.save(existingDevice);
        return deviceMapper.toResponseDTO(updatedDevice);
    }

    @Override
    public DeviceResponseDTO getDeviceById(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));
        return deviceMapper.toResponseDTO(device);
    }

    @Override
    public List<DeviceResponseDTO> getMyYardDevices() {
        var currentUser = accountService.getCurrentUser();

        if (currentUser.getScrapYard() == null) {
            throw new InvalidRequestException("You are not assigned to any scrap yard");
        }

        List<Device> devices = deviceRepository.findByScrapYardYardId(currentUser.getScrapYard().getYardId());
        return deviceMapper.toResponseDTOList(devices);
    }

    @Override
    public List<DeviceResponseDTO> getDevicesByYardId(Long yardId) {
        List<Device> devices = deviceRepository.findByScrapYardYardId(yardId);
        return deviceMapper.toResponseDTOList(devices);
    }

    @Override
    public Page<DeviceResponseDTO> getAllDevices(Pageable pageable) {
        Page<Device> devicePage = deviceRepository.findAll(pageable);
        return devicePage.map(deviceMapper::toResponseDTO);
    }

    @Override
    public void deleteDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        validateYardOwnership(device);

        deviceRepository.deleteById(deviceId);
    }

    @Override
    public boolean existsById(Long deviceId) {
        return deviceRepository.existsById(deviceId);
    }

    @Override
    public DeviceResponseDTO regenerateApiKey(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        validateYardOwnership(device);

        String rawKey = deviceApiKeyService.generateRawKey();
        device.setApiKeyHash(deviceApiKeyService.hash(rawKey));
        device.setApiKeyPrefix(deviceApiKeyService.extractPrefix(rawKey));
        device.setApiKeyRotatedAt(LocalDateTime.now());
        // The old key stops working the moment this save() commits — no
        // grace period, since a rotation is normally triggered because the
        // old key is suspected compromised.

        Device savedDevice = deviceRepository.save(device);

        DeviceResponseDTO response = deviceMapper.toResponseDTO(savedDevice);
        response.setApiKey(rawKey);   // shown once — caller must save it now
        return response;
    }

    @Override
    public DeviceResponseDTO updateStatus(Long deviceId, DeviceStatus status) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));

        validateYardOwnership(device);

        device.setStatus(status);
        Device savedDevice = deviceRepository.save(device);
        return deviceMapper.toResponseDTO(savedDevice);
    }

    // ==================== Private Helper ====================

    private void validateYardOwnership(Device device) {
        var currentUser = accountService.getCurrentUser();

        if (currentUser.getScrapYard() == null) {
            throw new InvalidRequestException("You are not assigned to any scrap yard");
        }

        if (device.getScrapYard() == null ||
                !device.getScrapYard().getYardId().equals(currentUser.getScrapYard().getYardId())) {

            throw new InvalidRequestException("You can only manage devices in your own scrap yard");
        }
    }
}