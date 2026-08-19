package com.scrapDetection.service.detection;

import com.scrapDetection.dto.detection.DetectionResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LatestDetectionStoreTest {

    private final LatestDetectionStore store = new LatestDetectionStore();

    @Test
    void get_returnsOnlyTheRequestedDevicesDetection() {
        DetectionResponseDTO first = detection(1L, "paper", "2026-08-07T08:00:01Z");
        DetectionResponseDTO second = detection(2L, "copper", "2026-08-07T08:00:02Z");

        store.set(1L, first);
        store.set(2L, second);

        assertEquals(first, store.get(1L, null));
        assertEquals(second, store.get(2L, null));
        assertNull(store.get(3L, null));
    }

    @Test
    void get_returnsNullWhenDetectionIsNotNewerThanAfter() {
        DetectionResponseDTO response = detection(1L, "paper", "2026-08-07T08:00:01Z");
        store.set(1L, response);

        assertNull(store.get(1L, Instant.parse("2026-08-07T08:00:01Z")));
        assertNull(store.get(1L, Instant.parse("2026-08-07T08:00:02Z")));
        assertEquals(response, store.get(1L, Instant.parse("2026-08-07T08:00:00Z")));
    }

    private DetectionResponseDTO detection(Long deviceId, String className, String receivedAt) {
        return DetectionResponseDTO.builder()
                .status("received")
                .deviceId(deviceId)
                .className(className)
                .receivedAt(Instant.parse(receivedAt))
                .build();
    }
}
