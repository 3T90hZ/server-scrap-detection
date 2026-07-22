package com.scrapDetection.service;

import com.scrapDetection.dto.account.*;
import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Role;

import java.util.List;

public interface AccountService {

    // Customer
    AuthResponseDTO registerCustomer(CreateAccountRequestDTO request, Long yardId);

    // Authentication
    AuthResponseDTO login(LoginRequestDTO request);

    AuthResponseDTO adminLogin(LoginRequestDTO request);

    // Yard Owner Operations
    AuthResponseDTO createStaff(CreateAccountRequestDTO request);

    Account getCurrentUser(); // For security context

    // Update
    AuthResponseDTO updateAccount(Long accountId, AccountUpdateRequestDTO request);

    // Password Reset
    void requestPasswordReset(PasswordResetRequestDTO request);

    AuthResponseDTO resetPassword(PasswordResetConfirmDTO request);

    // Utility
    List<AccountInfoResponseDTO> getAllStaffByYardOwner();
    AccountInfoResponseDTO updateAccountStatus(Long currentAccountId,ChangeAccountStatusRequestDTO dto);

    void logout(String token);

    void changeRole(Long yardId, Role fromRole, Role toRole);
}