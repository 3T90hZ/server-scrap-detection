package com.scrapDetection.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private Long accountId;
    private String accountName;
    private String role;
    private String phoneNumbers;
    private String email;
    private String status;
    private String token;           // JWT token
    private LocalDateTime expiresAt;
}