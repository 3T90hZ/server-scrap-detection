package com.scrapDetection.service.detection;

import com.scrapDetection.dto.detection.DetectionResponseDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
  In-memory "latest detection" holder keyed by authenticated device ID.
  No DB writes and no history: each camera retains only its most recent value
  so one yard/device cannot overwrite the result being viewed by another.

  Holds a DetectionResponseDTO directly — the same shape already sent back
  to the Pi — rather than a separate read-only DTO, so there's only one
  detection response shape in the codebase.

  DetectionServiceImpl writes after successful processing; DetectionController
  reads by device ID and optionally filters by the server-side receivedAt time.
 */
@Component
public class LatestDetectionStore {

    private final ConcurrentMap<Long, DetectionResponseDTO> latestByDevice = new ConcurrentHashMap<>();

    public void set(Long deviceId, DetectionResponseDTO response) {
        latestByDevice.put(deviceId, response);
    }

    /* Returns this device's latest detection only when it is newer than after. */
    public DetectionResponseDTO get(Long deviceId, Instant after) {
        DetectionResponseDTO latest = latestByDevice.get(deviceId);
        if (latest == null) {
            System.out.println("No latest detection found for device id: " + deviceId);
            return null;
        }
        if (after != null && (latest.getReceivedAt() == null || !latest.getReceivedAt().isAfter(after))) {
            System.out.println("No new latest detection for device id: " + deviceId);
            return null;
        }
        return latest;
    }
}
