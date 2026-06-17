package com.scrapDetection.service.impl;

import com.scrapDetection.dto.account.*;
import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Role;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.exception.ResourceAlreadyExistsException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.AccountMapper;
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.repository.ScrapYardRepository;
import com.scrapDetection.security.jwt.JwtService;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ScrapYardRepository scrapYardRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionService sessionService;

    @Override
    public AuthResponseDTO registerCustomer(CustomerRegisterRequestDTO request) {
        validateUniqueFields(request.getPhoneNumbers(), request.getEmail());

        Account account = accountMapper.toEntity(request);
        account.setRole(Role.CUSTOMER);
        account.setStatus("ACTIVE");
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        Account saved = accountRepository.save(account);

        String token = jwtService.generateToken(saved);
        sessionService.createSession(saved, token);

        return accountMapper.toAuthResponse(saved, token);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        Account account = accountRepository.findByPhoneNumbersAndStatus(request.getPhoneNumbers(), "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Account", "phoneNumbers", request.getPhoneNumbers()));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new InvalidRequestException("Invalid phone number or password");
        }

        String token = jwtService.generateToken(account);
        sessionService.createSession(account, token);

        return accountMapper.toAuthResponse(account, token);
    }

    @Override
    public AuthResponseDTO createYardOwner(YardOwnerCreateRequestDTO request) {
        validateUniqueFields(request.getPhoneNumbers(), request.getEmail());

        Account account = accountMapper.toEntity(request);
        account.setRole(Role.YARD_OWNER);
        account.setStatus("ACTIVE");
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (request.getYardId() != null) {
            var yard = scrapYardRepository.findById(request.getYardId())
                    .orElseThrow(() -> new ResourceNotFoundException("ScrapYard", request.getYardId()));
            account.setScrapYard(yard);
        }

        Account saved = accountRepository.save(account);
        String token = jwtService.generateToken(saved);
        sessionService.createSession(saved, token);

        return accountMapper.toAuthResponse(saved, token);
    }

    @Override
    public AuthResponseDTO createStaff(StaffCreateRequestDTO request) {
        validateUniqueFields(request.getPhoneNumbers(), request.getEmail());

        Account account = accountMapper.toEntity(request);
        account.setRole(Role.STAFF);
        account.setStatus("ACTIVE");
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        Account currentUser = getCurrentUser();
        if (currentUser.getScrapYard() == null) {
            throw new InvalidRequestException("Yard owner must be assigned to a scrap yard");
        }
        account.setScrapYard(currentUser.getScrapYard());

        Account saved = accountRepository.save(account);
        return accountMapper.toAuthResponse(saved, null);
    }

    @Override
    public Account getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidRequestException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Account account) {
            return account;
        }

        throw new InvalidRequestException("User not authenticated");
    }

    @Override
    public AuthResponseDTO updateAccount(Long accountId, AccountUpdateRequestDTO request) {
        Account existing = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        if (request.getPhoneNumbers() != null &&
                !existing.getPhoneNumbers().equals(request.getPhoneNumbers()) &&
                accountRepository.existsByPhoneNumbers(request.getPhoneNumbers())) {

            throw new ResourceAlreadyExistsException("Account", "phoneNumbers", request.getPhoneNumbers());
        }

        accountMapper.updateEntityFromDTO(request, existing);
        Account updated = accountRepository.save(existing);

        return accountMapper.toAuthResponse(updated, null);
    }

    @Override
    public void requestPasswordReset(PasswordResetRequestDTO request) {
        if (request.getPhoneNumbers() == null && request.getEmail() == null) {
            throw new InvalidRequestException("Phone number or email is required");
        }
        // TODO: Implement OTP logic later
    }

    @Override
    public AuthResponseDTO resetPassword(PasswordResetConfirmDTO request) {
        // TODO: Implement full reset logic with token verification
        throw new UnsupportedOperationException("Password reset flow to be implemented");
    }

    @Override
    public List<Account> getAllStaffByYardOwner() {
        Account current = getCurrentUser();
        if (current.getScrapYard() == null) {
            throw new InvalidRequestException("Current user is not associated with any scrap yard");
        }
        return accountRepository.findByScrapYardYardIdAndRole(
                current.getScrapYard().getYardId(), Role.STAFF);
    }

    @Override
    public void deactivateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        account.setStatus("INACTIVE");
        accountRepository.save(account);
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        sessionService.invalidateSession(token);
    }

    // ==================== Helper Methods ====================

    private void validateUniqueFields(String phone, String email) {
        if (accountRepository.existsByPhoneNumbers(phone)) {
            throw new ResourceAlreadyExistsException("Account", "phoneNumbers", phone);
        }
        if (email != null && !email.trim().isEmpty() && accountRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Account", "email", email);
        }
    }
}