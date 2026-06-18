package com.scrapDetection.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "scrap_yards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapYard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "yard_id")
    private Long yardId;

    @Column(name = "yard_name", nullable = false, unique = true, length = 150)
    private String yardName;

    @Column(name = "phone_numbers", unique = true, nullable = false)
    private String phoneNumbers;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "scrapYard", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Material> materials = new HashSet<>();

    @OneToMany(mappedBy = "scrapYard", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Device> devices = new HashSet<>();

    @OneToMany(mappedBy = "scrapYard", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Account> accounts = new HashSet<>();
}