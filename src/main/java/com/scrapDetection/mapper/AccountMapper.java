package com.scrapDetection.mapper;

import com.scrapDetection.dto.account.*;
import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountMapper {

    public Account toEntity(CustomerRegisterRequestDTO dto) {
        Account account = new Account();
        account.setAccountName(dto.getAccountName());
        account.setPhoneNumbers(dto.getPhoneNumbers());
        account.setEmail(dto.getEmail());
        return account;
    }

    public Account toEntity(YardOwnerCreateRequestDTO dto) {
        Account account = new Account();
        account.setAccountName(dto.getAccountName());
        account.setPhoneNumbers(dto.getPhoneNumbers());
        account.setEmail(dto.getEmail());
        return account;
    }

    public Account toEntity(StaffCreateRequestDTO dto) {
        Account account = new Account();
        account.setAccountName(dto.getAccountName());
        account.setPhoneNumbers(dto.getPhoneNumbers());
        account.setEmail(dto.getEmail());
        return account;
    }

    public void updateEntityFromDTO(AccountUpdateRequestDTO dto, Account entity) {
        if (dto.getAccountName() != null) {
            entity.setAccountName(dto.getAccountName());
        }
        if (dto.getPhoneNumbers() != null) {
            entity.setPhoneNumbers(dto.getPhoneNumbers());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }

    public AuthResponseDTO toAuthResponse(Account account, String token) {
        return AuthResponseDTO.builder()
                .accountId(account.getAccountId())
                .accountName(account.getAccountName())
                .phoneNumbers(account.getPhoneNumbers())
                .email(account.getEmail())
                .role(account.getRole() != null ? account.getRole().name() : null)
                .status(account.getStatus())
                .token(token)
                .expiresAt(account.getCreatedAt().plusHours(24))
                .build();
    }

    // For listing staff / users
    public List<AuthResponseDTO> toAuthResponseList(List<Account> accounts) {
        return accounts.stream()
                .map(acc -> toAuthResponse(acc, null))
                .toList();
    }
}