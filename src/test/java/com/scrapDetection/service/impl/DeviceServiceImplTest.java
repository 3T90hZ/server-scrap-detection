package com.scrapDetection.service.impl;

import com.scrapDetection.dto.device.DeviceViewerAccessDTO;
import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Device;
import com.scrapDetection.entity.DeviceStatus;
import com.scrapDetection.entity.Role;
import com.scrapDetection.entity.ScrapYard;
import com.scrapDetection.exception.ForbiddenException;
import com.scrapDetection.mapper.DeviceMapper;
import com.scrapDetection.repository.DeviceRepository;
import com.scrapDetection.security.device.DeviceApiKeyService;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private DeviceMapper deviceMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private DeviceApiKeyService deviceApiKeyService;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    @Test
    void getViewerAccess_allowsActiveStaffForActiveDeviceInSameYard() {
        ScrapYard yard = ScrapYard.builder().yardId(10L).build();
        Account staff = Account.builder()
                .accountId(20L)
                .role(Role.STAFF)
                .status("ACTIVE")
                .scrapYard(yard)
                .build();
        Device device = Device.builder()
                .deviceId(30L)
                .status(DeviceStatus.ACTIVE)
                .scrapYard(yard)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(staff);
        when(deviceRepository.findById(30L)).thenReturn(Optional.of(device));

        DeviceViewerAccessDTO access = deviceService.getViewerAccess(30L);

        assertEquals(20L, access.getAccountId());
        assertEquals(Role.STAFF, access.getRole());
        assertEquals(30L, access.getDeviceId());
    }

    @Test
    void getViewerAccess_rejectsViewerFromAnotherYard() {
        Account owner = Account.builder()
                .accountId(20L)
                .role(Role.YARD_OWNER)
                .status("ACTIVE")
                .scrapYard(ScrapYard.builder().yardId(10L).build())
                .build();
        Device device = Device.builder()
                .deviceId(30L)
                .status(DeviceStatus.ACTIVE)
                .scrapYard(ScrapYard.builder().yardId(11L).build())
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(deviceRepository.findById(30L)).thenReturn(Optional.of(device));

        assertThrows(ForbiddenException.class, () -> deviceService.getViewerAccess(30L));
    }

    @Test
    void getViewerAccess_rejectsInactiveDevice() {
        ScrapYard yard = ScrapYard.builder().yardId(10L).build();
        Account owner = Account.builder()
                .accountId(20L)
                .role(Role.YARD_OWNER)
                .status("ACTIVE")
                .scrapYard(yard)
                .build();
        Device device = Device.builder()
                .deviceId(30L)
                .status(DeviceStatus.DISABLED)
                .scrapYard(yard)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(deviceRepository.findById(30L)).thenReturn(Optional.of(device));

        assertThrows(ForbiddenException.class, () -> deviceService.getViewerAccess(30L));
    }

    @Test
    void getViewerAccess_rejectsInactiveAccount() {
        ScrapYard yard = ScrapYard.builder().yardId(10L).build();
        Account staff = Account.builder()
                .accountId(20L)
                .role(Role.STAFF)
                .status("INACTIVE")
                .scrapYard(yard)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(staff);

        assertThrows(ForbiddenException.class, () -> deviceService.getViewerAccess(30L));
    }
}
