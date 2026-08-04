package com.scrapDetection.controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    private volatile byte[]  latestFrame     = null;
    private volatile String  latestTimestamp = null;

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

            synchronized (this) {
                latestFrame     = bytes;
                latestTimestamp = ts;
            }

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
    public ResponseEntity<byte[]> getLatestFrame() {
        byte[] frame = latestFrame;   // read volatile once
        if (frame == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache")
                .header("X-Frame-Timestamp", latestTimestamp)
                .body(frame);
    }
}
