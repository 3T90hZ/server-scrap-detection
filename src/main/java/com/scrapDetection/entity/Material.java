package com.scrapDetection.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long materialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yard_id", nullable = false)
    private ScrapYard scrapYard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialStatus status;

    @Column(name = "icon",nullable = false)
    private String icon;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_price", nullable = false)
    private Double itemPrice;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "stock", nullable = false)
    @Builder.Default
    private Double stock = 0D;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "material")
    @Builder.Default
    private Set<Transaction> transactions = new HashSet<>();

    @OneToOne(mappedBy = "material")
    private Label label;
}
