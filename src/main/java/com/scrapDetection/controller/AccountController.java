package com.scrapDetection.controller;

import com.scrapDetection.dto.account.*;
import com.scrapDetection.entity.Account;
import com.scrapDetection.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/register/customer")
    public ResponseEntity<AuthResponseDTO> registerCustomer(
            @Valid @RequestBody CustomerRegisterRequestDTO request) {

        AuthResponseDTO response = accountService.registerCustomer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = accountService.login(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/yard-owner")
    public ResponseEntity<AuthResponseDTO> createYardOwner(@Valid @RequestBody YardOwnerCreateRequestDTO request) {
        AuthResponseDTO response = accountService.createYardOwner(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('YARD_OWNER')")
    @PostMapping("/yard-owner/staff")
    public ResponseEntity<AuthResponseDTO> createStaff(@Valid @RequestBody StaffCreateRequestDTO request) {
        AuthResponseDTO response = accountService.createStaff(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/account")
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

    @PostMapping("/password-reset/request")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        accountService.requestPasswordReset(request);
        return ResponseEntity.ok("Password reset instructions sent.");
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetConfirmDTO request) {
        accountService.resetPassword(request);
        return ResponseEntity.ok("Password has been reset successfully.");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            accountService.logout(token);
        }
        return ResponseEntity.ok("Logged out successfully.");
    }

    @PreAuthorize("hasRole('YARD_OWNER')")
    @GetMapping("/yard-owner/staff")
    public ResponseEntity<List<Account>> getMyStaff() {
        List<Account> staff = accountService.getAllStaffByYardOwner();
        return ResponseEntity.ok(staff);
    }

    // Helper method
    private Long getCurrentUserId() {
        Account currentUser = accountService.getCurrentUser();
        return currentUser.getAccountId();
    }
}