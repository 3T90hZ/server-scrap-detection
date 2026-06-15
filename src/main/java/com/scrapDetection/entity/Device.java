package com.scrapDetection.entity;

import jakarta.persistence.*;
import lombok.*;

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
}
