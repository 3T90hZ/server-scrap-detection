package com.scrapDetection.controller;

import com.scrapDetection.dto.detection.DetectionRequestDTO;
import com.scrapDetection.dto.detection.DetectionResponseDTO;
import com.scrapDetection.service.DetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 Endpoint
   POST /api/detections
 Auth (v1)
   Public — no JWT required.
 *
 Success  → 201 Created  + DetectionResponse(status="ok", transactionId, ...)
 No match → 422 Unprocessable  + DetectionResponse(status="error", message)
 Bad JSON → 400 Bad Request    (handled by Spring automatically)
 */

@Slf4j
@RestController
@RequestMapping("/api/detections")
@RequiredArgsConstructor
public class DetectionController {
    private final DetectionService detectionService;

    /*
     Receive a detection payload from the Pi and create a pending transaction.

     The Pi sends this automatically after every stable weight + inference
     event (see api_client.py → send_event()).
     */

    @PostMapping
    public ResponseEntity<DetectionResponseDTO> receiveDetection(
            @RequestBody DetectionRequestDTO request) {

        log.info("[DetectionController] POST /api/detections — "
                        + "timestamp={} weight_g={} detections={}",
                request.getTimestamp(),
                request.getWeightG(),
                request.getDetections() == null ? 0
                        : request.getDetections().size());

        DetectionResponseDTO response = detectionService.processDetection(request);

        if ("ok".equals(response.getStatus())) {
            return ResponseEntity.status(201).body(response);
        } else {
            // 422: the request was well-formed but the business logic couldn't
            // process it (unknown material, empty detections, etc.)
            return ResponseEntity.unprocessableEntity().body(response);
        }
    }
}
