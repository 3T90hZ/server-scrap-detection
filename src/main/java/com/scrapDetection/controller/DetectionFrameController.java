package com.scrapDetection.controller;

import com.scrapDetection.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.scrapDetection.entity.Device;
import com.scrapDetection.entity.Account;

/*
  Receives the annotated inference JPEG from the Raspberry Pi.

  POST /api/detections/frame
    Content-Type: multipart/form-data
    field name  : "frame"  (JPEG bytes)

  V1 behaviour — receive, log, and hold in memory.
 */

@Slf4j
@RestController
@RequestMapping("/api/detections/frame")
public class DetectionFrameController {
    // ── In-memory latest frame ─────────────────────────────────────────────
    private final Map<Long, byte[]> latestFrames = new ConcurrentHashMap<>();
    private final Map<Long, String> latestTimestamps = new ConcurrentHashMap<>();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── POST: receive frame from Pi ────────────────────────────────────────

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> receiveFrame(
            @RequestParam("frame") MultipartFile file) {

        if (file.isEmpty()) {
            log.warn("[DetectionFrameController] Received empty frame upload.");
            return ResponseEntity.badRequest().body("Frame file is empty.");
        }

        try {
            byte[] bytes = file.getBytes();
            String ts    = LocalDateTime.now().format(FMT);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Device device = (Device) auth.getPrincipal();
            Long yardId = device.getScrapYard().getYardId();

            latestFrames.put(yardId, bytes);
            latestTimestamps.put(yardId, ts);

            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("[DetectionFrameController] Inference frame received");
            log.info("  timestamp : {}", ts);
            log.info("  size      : {} KB", bytes.length / 1024);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            return ResponseEntity.ok("Frame received at " + ts);

        } catch (IOException e) {
            log.error("[DetectionFrameController] Failed to read uploaded frame: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to read frame.");
        }
    }

    // ── GET: serve latest annotated frame to frontend ────────────────────────────────

    /*
      Returns the most recently received annotated JPEG as image/jpeg.
      The frontend can call this endpoint to display the frozen inference frame.

      200 image/jpeg  — latest annotated frame
      204 No Content  — no frame received yet
     */
    @GetMapping(produces = "image/jpeg")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<byte[]> getLatestFrame() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = (Account) auth.getPrincipal();
        Long yardId = account.getScrapYard().getYardId();

        byte[] frame = latestFrames.get(yardId);
        if (frame == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache")
                .header("X-Frame-Timestamp", latestTimestamps.get(yardId))
                .body(frame);
    }
}
