package com.scrapDetection.controller;

import com.scrapDetection.dto.account.AccountInfoResponseDTO;
import com.scrapDetection.dto.account.AccountUpdateRequestDTO;
import com.scrapDetection.dto.account.AuthResponseDTO;
import com.scrapDetection.entity.Account;
import com.scrapDetection.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PreAuthorize("hasRole('YARD_OWNER')")
    @GetMapping("/staff")
    public ResponseEntity<List<AccountInfoResponseDTO>> getMyStaff() {
        List<AccountInfoResponseDTO> staff = accountService.getAllStaffByYardOwner();
        return ResponseEntity.ok(staff);
    }
    @PreAuthorize("hasRole('YARD_OWNER')")
    @PutMapping()
    public ResponseEntity<AuthResponseDTO> updateStaffAccount(@Valid @RequestBody AccountUpdateRequestDTO request) {
        Long currentUserId = getCurrentUserId();
        AuthResponseDTO response = accountService.updateAccount(currentUserId, request);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("isAuthenticated()")
    @PutMapping()
    public ResponseEntity<AuthResponseDTO> updateAccount(@Valid @RequestBody AccountUpdateRequestDTO request) {
        Long currentUserId = getCurrentUserId();
        AuthResponseDTO response = accountService.updateAccount(currentUserId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<Account> getCurrentUserProfile() {
        Account currentUser = accountService.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }
    // Helper method
    private Long getCurrentUserId() {
        Account currentUser = accountService.getCurrentUser();
        return currentUser.getAccountId();
    }
}
