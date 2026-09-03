package com.scrapDetection.service.impl;

import com.scrapDetection.dto.detection.DetectionRequestDTO;
import com.scrapDetection.dto.detection.DetectionRequestDTO.DetectionItemDTO;
import com.scrapDetection.dto.detection.DetectionResponseDTO;
import com.scrapDetection.entity.*;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.mapper.DetectionMapper;
import com.scrapDetection.repository.DeviceRepository;
import com.scrapDetection.repository.LabelRepository;
import com.scrapDetection.service.CurrentUserService;
import com.scrapDetection.service.DetectionService;
import com.scrapDetection.service.detection.LatestDetectionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.time.Instant;

/*
  V1 — receive and log only, no DB writes.

  Flow:
    1. Validate the payload has at least one detection item.
    2. Pick the detection with the highest confidence.
    3. Log everything clearly so the backend console confirms the Pi is talking.
    4. Stamp the response with the authenticated device ID and server receipt
       time, then store it in LatestDetectionStore so the frontend can poll via
       GET /api/detections (see DetectionController).
    5. Return the (now timestamped) response — no Transaction, no Material
       lookup.

  Note: weight_g is baseline-relative on the Pi side, i.e. the actual
  weight of whatever is currently on the scale (Pi subtracts its own idle
  baseline before sending) — not a raw zero-referenced sensor reading.

  Business logic (Transaction creation, Material matching, etc.)
  will be handled by a separate API that calls this data as input.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DetectionServiceImpl implements DetectionService {
    private final DetectionMapper detectionMapper;
    private final LatestDetectionStore latestDetectionStore;
    private final LabelRepository  labelRepository;
    private final DeviceRepository deviceRepository;

    @Override
    public DetectionResponseDTO processDetection(Long deviceId, DetectionRequestDTO requestDTO) {

        List<DetectionItemDTO> detections = requestDTO.getDetections();

        // ── 1. Validate ───────────────────────────────────────────────────────
        if (detections == null || detections.isEmpty()) {
            log.warn("[DetectionService] Received payload with no detections — timestamp={}",
                    requestDTO.getTimestamp());
            return detectionMapper.toErrorResponse("Payload contained no detections.");
        }

        // ── 2. Pick highest-confidence detection ──────────────────────────────
        DetectionItemDTO best = detections.stream()
                .filter(d -> d.getClassName() != null && d.getConfidence() != null)
                .max(Comparator.comparingDouble(DetectionItemDTO::getConfidence))
                .orElse(null);

        if (best == null) {
            log.warn("[DetectionService] All detection items were missing class_name or confidence.");
            return detectionMapper.toErrorResponse(
                    "Detection items had no valid class_name / confidence.");
        }

        // ── 3. Log receipt ────────────────────────────────────────────────────
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("[DetectionService] Payload received from Pi");
        log.info("  timestamp       : {}", requestDTO.getTimestamp());
        log.info("  weight_g        : {} g",  requestDTO.getWeightG());
        log.info("  total detections: {}", detections.size());
        log.info("  best class      : {}", best.getClassName());
        log.info("  best confidence : {}", best.getConfidence());
        log.info("  weight_above_ref: {} g", requestDTO.getWeightAboveRefG());
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) {
            throw new InvalidRequestException("Device not found");
        }
        ScrapYard yard = device.getScrapYard();
        if(yard == null || !yard.getStatus().equals(YardStatus.ACTIVE)){
            throw new InvalidRequestException("Device is required to belong to an active yard");
        }
        Label label = labelRepository
                .findByScrapYardYardIdAndLabel(yard.getYardId(),best.getClassName())
                .orElse(null);
        // ── 4. Build response, stamp timestamp, store as "latest" ──────────────
        DetectionResponseDTO response = detectionMapper.toReceivedResponse(
                best.getClassName(),
                best.getConfidence(),
                requestDTO.getWeightG()
        );
        if (label != null) {
            Material material = label.getMaterial();
            if(material.getStatus().equals(MaterialStatus.ACTIVE)){
                response.setMaterialId(material.getMaterialId());
                response.setMaterialName(material.getItemName());
                response.setMaterialPrice(material.getItemPrice());
            }
        }
        response.setTimestamp(requestDTO.getTimestamp());
        response.setDeviceId(deviceId);
        response.setReceivedAt(Instant.now());
        log.info("deviceId: " + deviceId);
        latestDetectionStore.set(deviceId, response);
        // ── 5. Return success — no DB write ───────────────────────────────────
        return response;
    }
}
