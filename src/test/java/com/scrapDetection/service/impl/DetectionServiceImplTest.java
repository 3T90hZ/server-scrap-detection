package com.scrapDetection.service.impl;

import com.scrapDetection.dto.detection.DetectionRequestDTO;
import com.scrapDetection.dto.detection.DetectionRequestDTO.DetectionItemDTO;
import com.scrapDetection.dto.detection.DetectionResponseDTO;
import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.ScrapYard;
import com.scrapDetection.entity.YardStatus;
import com.scrapDetection.mapper.DetectionMapper;
import com.scrapDetection.repository.LabelRepository;
import com.scrapDetection.service.CurrentUserService;
import com.scrapDetection.service.detection.LatestDetectionStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetectionServiceImplTest {

    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private DetectionMapper detectionMapper;
    @Mock
    private LatestDetectionStore latestDetectionStore;

    @Mock
    private LabelRepository labelRepository;
    @Mock
    private Account account;
    @Mock
    private ScrapYard yard;

    @InjectMocks
    private DetectionServiceImpl detectionService;

    @Test
    void processDetection_stampsAuthenticatedDeviceAndServerReceiptTimeBeforeStoring() {
        DetectionItemDTO item = new DetectionItemDTO();
        item.setClassName("paper");
        item.setConfidence(0.9);
        DetectionRequestDTO request = new DetectionRequestDTO();
        request.setTimestamp("2026-08-07T08:00:00Z");
        request.setWeightG(1250.0);
        request.setDetections(List.of(item));
        DetectionResponseDTO response = DetectionResponseDTO.received("paper", 0.9, 1250.0, null, null, null, null);

        when(currentUserService.getCurrentUser()).thenReturn(account);
        when(account.getScrapYard()).thenReturn(yard);

        when(yard.getYardId()).thenReturn(1L);

        when(yard.getStatus()).thenReturn(YardStatus.ACTIVE);
        when(labelRepository
                .findByScrapYardYardIdAndLabel(1L, "paper"))
                .thenReturn(Optional.empty());

        when(detectionMapper.toReceivedResponse(
                "paper",
                0.9,
                1250.0,
                null, null, null, null
        )).thenReturn(response);
        DetectionResponseDTO result =
                detectionService.processDetection(42L, request);
        assertNotNull(result);

        assertEquals(42L, result.getDeviceId());
        assertEquals("2026-08-07T08:00:00Z", result.getTimestamp());
        assertNotNull(result.getReceivedAt());
        verify(latestDetectionStore).set(42L, result);
    }
}