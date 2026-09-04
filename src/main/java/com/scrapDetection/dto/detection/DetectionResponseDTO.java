package com.scrapDetection.dto.detection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/*
  Response sent back to the Raspberry Pi after processing a detection event.
  Also reused a for GET /api/detections (the frontend's polling
  endpoint — see DetectionController / LatestDetectionStore)

  On receipt (v1 — no transaction yet):
  {
    "status"      : "received",
    "className"   : "paper",
    "confidence"  : 0.923,
    "weightG"     : 312.5,
    "timestamp"   : "2025-07-01T14:32:01.123456",
    "deviceId"    : 12,
    "receivedAt"  : "2026-08-07T08:00:00.123Z",
    "bboxX1"      : 10,
    "bboxY1"      : 20,
    "bboxX2"      : 200,
    "bboxY2"      : 300
  }

  On error (bad payload):
  {
    "status"  : "error",
    "message" : "Payload contained no detections."
  }
 */

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetectionResponseDTO {
    private String status;          // "received" | "error"
    private String className;    // best detection class name echoed back
    private Long materialId;
    private String materialName;
    private Double materialPrice;
    private Double confidence;   // best detection confidence echoed back
    private Double weightG;      // weight from the payload echoed back
    private String message;
    private String timestamp;    // echoed from the Pi's request — lets pollers
    private Long deviceId;       // authenticated device that submitted the result
    private Instant receivedAt;  // server receipt time used to reject stale scans

    // Bounding box of the best detection (Pi lores coordinates: 640×480)
    private Double bboxX1;
    private Double bboxY1;
    private Double bboxX2;
    private Double bboxY2;

    public static DetectionResponseDTO received(String className,
                                                Double confidence,
                                                Double weightG,
                                                Double bboxX1,
                                                Double bboxY1,
                                                Double bboxX2,
                                                Double bboxY2) {
        return DetectionResponseDTO.builder()
                .status("received")
                .className(className)
                .confidence(confidence)
                .weightG(weightG)
                .bboxX1(bboxX1)
                .bboxY1(bboxY1)
                .bboxX2(bboxX2)
                .bboxY2(bboxY2)
                .build();
    }

    public static DetectionResponseDTO error(String message) {
        return DetectionResponseDTO.builder()
                .status("error")
                .message(message)
                .build();
    }
}
