package com.scrapDetection.dto.detection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/*
  Response sent back to the Raspberry Pi after processing a detection event.

  On receipt (v1 — no transaction yet):
  {
    "status"      : "received",
    "className"   : "paper",
    "confidence"  : 0.923,
    "weightG"     : 312.5
  }

  On error (bad payload):
  {
    "status"  : "error",
    "message" : "Payload contained no detections."
  }
 */

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetectionResponseDTO {
    private String status;          // "received" | "error"
    private String className;    // best detection class name echoed back
    private Double confidence;   // best detection confidence echoed back
    private Double weightG;      // weight from the payload echoed back
    private String message;

    public static DetectionResponseDTO received(String className,
                                                Double confidence,
                                                Double weightG) {
        return DetectionResponseDTO.builder()
                .status("received")
                .className(className)
                .confidence(confidence)
                .weightG(weightG)
                .build();
    }

    public static DetectionResponseDTO error(String message) {
        return DetectionResponseDTO.builder()
                .status("error")
                .message(message)
                .build();
    }
}
