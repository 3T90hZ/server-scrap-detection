package com.scrapDetection.controller;

import com.scrapDetection.dto.account.*;
import com.scrapDetection.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AccountService accountService;

    @PostMapping("/register/customer")
    public ResponseEntity<AuthResponseDTO> registerCustomer(
            @Valid @RequestBody CreateAccountRequestDTO request) {

        AuthResponseDTO response = accountService.registerCustomer(request, null);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = accountService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/admin")
    public ResponseEntity<AuthResponseDTO> adminLogin(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = accountService.adminLogin(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('YARD_OWNER')")
    @PostMapping("/staff")
    public ResponseEntity<AuthResponseDTO> createStaff(@Valid @RequestBody CreateAccountRequestDTO request) {
        AuthResponseDTO response = accountService.createStaff(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
}