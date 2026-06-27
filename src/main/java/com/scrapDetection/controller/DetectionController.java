package com.scrapDetection.controller;

import com.scrapDetection.dto.detection.DetectionRequestDTO;
import com.scrapDetection.dto.detection.DetectionResponseDTO;
import com.scrapDetection.service.DetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
  POST /api/detections
    + 200 OK            payload received and logged successfully
    + 422 Unprocessable payload was well-formed JSON but invalid content
                         (no detections, missing fields, etc.)
    + 400 Bad Request   malformed JSON (handled automatically by Spring)
 */

@Slf4j
@RestController
@RequestMapping("/api/detections")
@RequiredArgsConstructor
public class DetectionController {
    private final DetectionService detectionService;

    @PostMapping
    public ResponseEntity<DetectionResponseDTO> receiveDetection(
            @RequestBody DetectionRequestDTO request) {

        log.info("[DetectionController] POST /api/detections — "
                        + "timestamp={} weight_g={} detections={}",
                request.getTimestamp(),
                request.getWeightG(),
                request.getDetections() == null ? 0 : request.getDetections().size());

        DetectionResponseDTO response = detectionService.processDetection(request);

        if ("received".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.unprocessableEntity().body(response);
        }
    }
}
