package com.scrapDetection.controller;

import com.scrapDetection.dto.detection.DetectionRequestDTO;
import com.scrapDetection.dto.detection.DetectionResponseDTO;
import com.scrapDetection.service.DetectionService;
import com.scrapDetection.service.DeviceService;
import com.scrapDetection.service.detection.LatestDetectionStore;
import com.scrapDetection.entity.Device;
import com.scrapDetection.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.Instant;

/*
  POST /api/detections   (device only — ROLE_DEVICE)
    + 200 OK            payload received and logged successfully
    + 422 Unprocessable payload was well-formed JSON but invalid content
                         (no detections, missing fields, etc.)
    + 400 Bad Request   malformed JSON (handled automatically by Spring)

  GET  /api/detections?deviceId=...&after=...   (staff / yard owner only)
    + 200 OK            requested camera has a detection newer than after
    + 204 No Content     no new detection for that camera
    Access uses the same role/yard/device-active checks as stream viewing.
 */

@Slf4j
@RestController
@RequestMapping("/api/detections")
@RequiredArgsConstructor
public class DetectionController {
    private final DetectionService detectionService;
    private final DeviceService deviceService;
    private final LatestDetectionStore latestDetectionStore;

    @PostMapping
    public ResponseEntity<DetectionResponseDTO> receiveDetection(
            @AuthenticationPrincipal Device device,
            @RequestBody DetectionRequestDTO request) {

        if (device == null) {
            throw new UnauthorizedException("Device authentication is required");
        }

        log.info("[DetectionController] POST /api/detections — "
                        + "timestamp={} weight_g={} detections={}",
                request.getTimestamp(),
                request.getWeightG(),
                request.getDetections() == null ? 0 : request.getDetections().size());

        DetectionResponseDTO response = detectionService.processDetection(device.getDeviceId(), request);

        if ("received".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.unprocessableEntity().body(response);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'YARD_OWNER')")
    public ResponseEntity<DetectionResponseDTO> getLatestDetection(
            @RequestParam Long deviceId,
            @RequestParam(required = false) Instant after) {
        deviceService.getViewerAccess(deviceId);
        DetectionResponseDTO latest = latestDetectionStore.get(deviceId, after);
        if (latest == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(latest);
    }
}
