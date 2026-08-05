package com.scrapDetection.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resaleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "factory_name", nullable = false)
    private String factoryName;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "account_id")
    private Account createdBy;

    @Column(nullable = false)
    private Double weight;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "resale", cascade = CascadeType.ALL, orphanRemoval = true)
    private ResaleTotal resaleTotal;
}