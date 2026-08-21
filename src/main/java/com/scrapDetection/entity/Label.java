package com.scrapDetection.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "label")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Label {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long labelId;

    @Column(name = "label", nullable = false, unique = true)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yard_id", nullable = false)
    private ScrapYard scrapYard;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false, unique = true)
    private Material  material;
}
