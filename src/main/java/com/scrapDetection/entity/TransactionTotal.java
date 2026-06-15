package com.scrapDetection.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transaction_totals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionTotal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionTotalId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "total_worth", nullable = false)
    private Double totalWorth;
}
