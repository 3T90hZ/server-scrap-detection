package com.scrapDetection.dto.detection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/*
 Response sent back to the Raspberry Pi after processing a detection event.

 On success:
 {
   "status"        : "ok",
   "transactionId" : 42,
   "materialName"  : "paper",
   "weightG"       : 312.5,
   "unitPrice"     : 85000.0
 }
 On failure:
 {
   "status"  : "error",
   "message" : "No material found matching class name: 'paper'"
 }
 */

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)   // omit null fields in JSON output
public class DetectionResponseDTO {
    private String status;          // "ok" | "error"

    // ── Success fields ────────────────────────────────────────────────────────
    private Long   transactionId;
    private String materialName;
    private Double weightG;
    private Double unitPrice;

    // ── Error field ───────────────────────────────────────────────────────────
    private String message;

    // ── Static factories ──────────────────────────────────────────────────────

    public static DetectionResponseDTO success(Long transactionId,
                                            String materialName,
                                            Double weightG,
                                            Double unitPrice) {
        return DetectionResponseDTO.builder()
                .status("ok")
                .transactionId(transactionId)
                .materialName(materialName)
                .weightG(weightG)
                .unitPrice(unitPrice)
                .build();
    }

    public static DetectionResponseDTO error(String message) {
        return DetectionResponseDTO.builder()
                .status("error")
                .message(message)
                .build();
    }
}
