package com.scrapDetection.dto.detection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/*
 Payload received from the Raspberry Pi after a weight-triggered inference.

  Example JSON:
  {
    "timestamp"           : "2025-07-01T14:32:01.123456",
    "weight_g"            : 280.0,
    "weight_above_ref_g"  : 112.5,
    "detections": [
      { "class_name": "copper", "confidence": 0.923,
        "bbox": { "x1": 10, "y1": 20, "x2": 200, "y2": 300 } }
    ]
  }

 weight_g is already baseline-relative on the Pi side — it's the actual
 weight of whatever is currently on the scale (the Pi subtracts its own
 idle baseline before sending), NOT a raw zero-referenced sensor reading.
 There used to be a separate weight_above_base_g field for this, but since
 weight_g now carries that meaning directly, weight_above_base_g has been
 removed from the payload.

 Only class_name, confidence, and weight_g are used in v1.
 The rest is stored/ignored now and available for later versions.
 */

@Getter
@Setter
@NoArgsConstructor
public class DetectionRequestDTO {
    private String timestamp;

    @JsonProperty("weight_g")
    private Double weightG;

    @JsonProperty("weight_above_ref_g")
    private Double weightAboveRefG;

    private List<DetectionItemDTO> detections;

    // ── Nested DTO ────────────────────────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DetectionItemDTO {

        @JsonProperty("class_name")
        private String className;

        private Double confidence;

        private BBox bbox;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class BBox {
            private Integer x1;
            private Integer y1;
            private Integer x2;
            private Integer y2;
        }
    }
}