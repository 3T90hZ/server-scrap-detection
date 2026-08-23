package com.scrapDetection.controller;

import com.scrapDetection.dto.account.*;
import com.scrapDetection.entity.Account;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.CurrentUserService;
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
    private final CurrentUserService currentUserService;

    @PreAuthorize("hasRole('YARD_OWNER')")
    @GetMapping("/staff")
    public ResponseEntity<List<AccountInfoResponseDTO>> getMyStaff() {
        List<AccountInfoResponseDTO> staff = accountService.getAllStaffByYardOwner();
        return ResponseEntity.ok(staff);
    }

    @PreAuthorize("hasRole('YARD_OWNER')")
    @PostMapping("/add-staff")
    public ResponseEntity<Void> addStaff(
            @Valid @RequestBody AddStaffRequestDTO request
    ) {
        accountService.addStaff(request.getPhoneNumber());
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'YARD_OWNER', 'STAFF', 'CUSTOMER')")
    @PutMapping()
    public ResponseEntity<AuthResponseDTO> updateAccount(@Valid @RequestBody AccountUpdateRequestDTO request) {
        Long currentUserId = getCurrentUserId();
        AuthResponseDTO response = accountService.updateAccount(currentUserId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'YARD_OWNER', 'STAFF', 'CUSTOMER')")
    @PutMapping("/status")
    public ResponseEntity<AccountInfoResponseDTO> updateAccountStatus(@Valid @RequestBody ChangeAccountStatusRequestDTO request) {
        Long currentUserId = getCurrentUserId();
        AccountInfoResponseDTO response = accountService.updateAccountStatus(currentUserId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'YARD_OWNER', 'STAFF', 'CUSTOMER')")
    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> getCurrentUserProfile() {
        AuthResponseDTO currentUserInfo = accountService.getMyInfo();
        return ResponseEntity.ok(currentUserInfo);
    }

    @PreAuthorize("hasAnyRole('YARD_OWNER', 'STAFF')")
    @GetMapping("/customer")
    public ResponseEntity<AccountInfoResponseDTO> getCustomer(@Valid @RequestBody GetPhoneNumberRequestDTO request) {
        AccountInfoResponseDTO customer = accountService.findAccountByPhoneNumber(request);
        return ResponseEntity.ok(customer);
    }
    // Helper method
    private Long getCurrentUserId() {
        Account currentUser = currentUserService.getCurrentUser();
        return currentUser.getAccountId();
    }
}
