package com.scrapDetection.service;

import com.scrapDetection.dto.account.*;
import com.scrapDetection.entity.Account;

import java.util.List;

public interface AccountService {

    // Customer
    AuthResponseDTO registerCustomer(CustomerRegisterRequestDTO request);

    // Authentication
    AuthResponseDTO login(LoginRequestDTO request);

    // Admin Operations
    AuthResponseDTO createYardOwner(YardOwnerCreateRequestDTO request);

    // Yard Owner Operations
    AuthResponseDTO createStaff(StaffCreateRequestDTO request);

    Account getCurrentUser(); // For security context

    // Update
    AuthResponseDTO updateAccount(Long accountId, AccountUpdateRequestDTO request);

    // Password Reset
    void requestPasswordReset(PasswordResetRequestDTO request);
    AuthResponseDTO resetPassword(PasswordResetConfirmDTO request);

    // Utility
    List<Account> getAllStaffByYardOwner();
    void deactivateAccount(Long accountId);

    void logout(String token);
}