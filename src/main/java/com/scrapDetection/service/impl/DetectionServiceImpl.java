package com.scrapDetection.service.impl;

import com.scrapDetection.dto.detection.DetectionRequestDTO;
import com.scrapDetection.dto.detection.DetectionRequestDTO.DetectionItemDTO;
import com.scrapDetection.dto.detection.DetectionResponseDTO;
import com.scrapDetection.mapper.DetectionMapper;
import com.scrapDetection.service.DetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/*
  V1 — receive and log only, no DB writes.

  Flow:
    1. Validate the payload has at least one detection item.
    2. Pick the detection with the highest confidence.
    3. Log everything clearly so the backend console confirms the Pi is talking.
    4. Return a success response — no Transaction, no Material lookup.

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

    @Override
    public DetectionResponseDTO processDetection(DetectionRequestDTO requestDTO) {

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

        // ── 4. Return success — no DB write ───────────────────────────────────
        return detectionMapper.toReceivedResponse(
                best.getClassName(),
                best.getConfidence(),
                requestDTO.getWeightG()
        );
    }
}