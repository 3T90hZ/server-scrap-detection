package com.scrapDetection.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resale_totals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResaleTotal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resaleTotalId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resale_id", nullable = false, unique = true)
    private Resale resale;

    @Column(name = "total_worth", nullable = false)
    private Double totalWorth;
}
