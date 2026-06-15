package com.scrapDetection.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(name = "yard_id", nullable = true)
    private Long yardId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // ADMIN, YARD_OWNER, STAFF

    @Column(name = "account_name", nullable = false, unique = true)
    private String accountName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone_numbers")
    private String phoneNumbers; // comma-separated or JSON

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Session> sessions = new HashSet<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private Set<Transaction> customerTransactions = new HashSet<>();

    @OneToMany(mappedBy = "ownerOrStaff", cascade = CascadeType.ALL)
    private Set<Transaction> staffTransactions = new HashSet<>();
}
