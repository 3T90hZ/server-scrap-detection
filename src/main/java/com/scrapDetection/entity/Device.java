package com.scrapDetection.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yard_id", nullable = false)
    private ScrapYard scrapYard;

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @Column(name = "ip_address")
    private String ipAddress;

    // ── Device authentication ────────────────────────────────────────────────
    // SHA-256 hash of the device's API key. The raw key is generated once at
    // creation/rotation time, shown to the caller exactly once, and never
    // stored anywhere — only this hash is persisted. See DeviceApiKeyService.
    @Column(name = "api_key_hash", nullable = false)
    private String apiKeyHash;

    // First few characters of the raw key. Safe to display in an admin UI so
    // a yard owner can tell keys apart ("which device is using key ab12cd..?")
    // without ever exposing the full secret.
    @Column(name = "api_key_prefix", nullable = false, length = 8)
    private String apiKeyPrefix;

    @Column(name = "api_key_rotated_at")
    private LocalDateTime apiKeyRotatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeviceStatus status;

    // ── OTA readiness (feature itself is implemented later) ───────────────────
    @Column(name = "firmware_version")
    private String firmwareVersion;          // version the device last reported

    @Column(name = "desired_firmware_version")
    private String desiredFirmwareVersion;   // version an admin wants pushed to it

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;        // updated on every authenticated request

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
