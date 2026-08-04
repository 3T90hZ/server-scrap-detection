package com.scrapDetection.service.detection;

import com.scrapDetection.dto.detection.DetectionResponseDTO;
import org.springframework.stereotype.Component;

/*
  V1 in-memory "latest detection" holder — same pattern already used by
  DetectionFrameController for the latest annotated JPEG (volatile field,
  synchronized write, single read). No DB writes, no history, just the
  most recent value so the frontend has something to poll.

  Holds a DetectionResponseDTO directly — the same shape already sent back
  to the Pi — rather than a separate read-only DTO, so there's only one
  detection response shape in the codebase.

  DetectionServiceImpl (writer) sets this after successfully processing a
  detection; DetectionController (reader) exposes it via GET /api/detections.
 */
@Component
public class LatestDetectionStore {

    private volatile DetectionResponseDTO latest = null;

    public void set(DetectionResponseDTO response) {
        this.latest = response;
    }

    /* Returns the latest known detection response, or null if none has arrived yet. */
    public DetectionResponseDTO get() {
        return latest;  // read volatile once
    }
}