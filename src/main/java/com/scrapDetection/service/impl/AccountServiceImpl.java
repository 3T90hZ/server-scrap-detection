package com.scrapDetection.service.impl;

import com.scrapDetection.dto.account.*;
import com.scrapDetection.entity.*;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.exception.InvalidTokenException;
import com.scrapDetection.exception.ResourceAlreadyExistsException;
import com.scrapDetection.exception.ResourceNotFoundException;
import com.scrapDetection.mapper.AccountMapper;
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.repository.PasswordResetTokenRepository;
import com.scrapDetection.repository.ScrapYardRepository;
import com.scrapDetection.security.jwt.JwtService;
import com.scrapDetection.service.AccountService;
import com.scrapDetection.service.EmailService;
import com.scrapDetection.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    private static final int TOKEN_EXPIRY_MINUTES = 60;

    @Override
    public AuthResponseDTO registerCustomer(CreateAccountRequestDTO request, Long yardId) {
        validateUniqueFields(request.getPhoneNumbers(), request.getEmail());

        Account account = accountMapper.toEntity(request);
        account.setRole(Role.CUSTOMER);
        account.setStatus("ACTIVE");
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if(yardId != null) {
            account.setScrapYard(scrapYardRepository.getReferenceById(yardId));
        }
        Account saved = accountRepository.save(account);

        String token = jwtService.generateToken(saved);
        sessionService.createSession(saved, token);

        return accountMapper.toAuthResponse(saved, token);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        Account account = accountRepository.findByPhoneNumbers(request.getPhoneNumbers())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "phoneNumbers", request.getPhoneNumbers()));

        if(account.getScrapYard() != null && account.getRole() != Role.CUSTOMER){
            ScrapYard scrapYard = scrapYardRepository.getReferenceById(account.getScrapYard().getYardId());
            if( !scrapYard.getStatus().equals("ACTIVE")){
                throw new InvalidRequestException("Yard is not activated");
            }
        }
        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new InvalidRequestException("Invalid phone number or password");
        }
        if(account.getStatus().equals("INACTIVE")){
            throw new InvalidRequestException("The account is locked!");
        }

        String token = jwtService.generateToken(account);
        sessionService.createSession(account, token);

        return accountMapper.toAuthResponse(account, token);
    }

    @Override
    public AuthResponseDTO adminLogin(LoginRequestDTO request) {
        Account account = accountRepository.findByPhoneNumbers(request.getPhoneNumbers())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "phoneNumbers", request.getPhoneNumbers()));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new InvalidRequestException("Invalid phone number or password");
        }
        if(account.getRole() != Role.ADMIN){
            throw new InvalidRequestException("No access");
        }

        String token = jwtService.generateToken(account);
        sessionService.createSession(account, token);

        return accountMapper.toAuthResponse(account, token);
    }


    @Override
    public AuthResponseDTO createStaff(CreateAccountRequestDTO request) {
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
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        accountMapper.updateEntityFromDTO(request, existing);
        Account updated = accountRepository.save(existing);

        return accountMapper.toAuthResponse(updated, null);
    }

    @Override
    public void requestPasswordReset(PasswordResetRequestDTO request) {
        if ((request.getPhoneNumbers() == null || request.getPhoneNumbers().trim().isEmpty()) &&
                (request.getEmail() == null || request.getEmail().trim().isEmpty())) {
            throw new InvalidRequestException("Phone number or email is required");
        }

        // Email-based password reset
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            Account account = accountRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + request.getEmail()));

            // Invalidate old tokens
            tokenRepository.deleteByAccount(account);

            // Create new token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .account(account)
                    .expiryDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))
                    .build();

            tokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(account.getEmail(), token);
        }

        // TODO: Phone OTP
        if (request.getPhoneNumbers() != null && !request.getPhoneNumbers().trim().isEmpty()) {

        }
    }

    @Override
    public AuthResponseDTO resetPassword(PasswordResetConfirmDTO request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getResetToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new InvalidTokenException("Reset token has expired. Please request a new one.");
        }

        Account account = resetToken.getAccount();

        // Update password
        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        // Invalidate old sessions (optional but recommended)
        sessionService.invalidateAllSessions(account.getAccountId());

        // Clean up token
        tokenRepository.delete(resetToken);

        return accountMapper.toAuthResponse(account, null);
    }

    @Override
    public List<AccountInfoResponseDTO> getAllStaffByYardOwner() {
        Account current = getCurrentUser();
        if (current.getScrapYard() == null) {
            throw new InvalidRequestException("Current user is not associated with any scrap yard");
        }
        List<Account> accounts = accountRepository.findByScrapYardYardIdAndRole(current.getScrapYard().getYardId(), Role.STAFF);
        return accountMapper.toAccountInfoResponseList(accounts);
    }

    @Override
    public AccountInfoResponseDTO updateAccountStatus(Long currentAccountId,ChangeAccountStatusRequestDTO dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", dto.getAccountId()));
        Account currentAccount = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", currentAccountId));

        if(currentAccount.getRole() == Role.YARD_OWNER){
            if(!Objects.equals(account.getScrapYard().getYardId(), currentAccount.getScrapYard().getYardId())){
                throw new InvalidRequestException("No permission");
            }
        }
        else {
            if(currentAccount.getRole() != Role.ADMIN && !dto.getAccountId().equals(currentAccountId)){
                throw new InvalidRequestException("No permission");
            }
            if(currentAccount.getRole() == Role.ADMIN && dto.getAccountId().equals(currentAccountId)){
                throw new InvalidRequestException("No permission");
            }
        }
        account.setStatus(dto.getStatus());
        return accountMapper.toAccountInfoResponse(accountRepository.save(account));
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
    @Override
    public void changeRole(Long yardId, Role fromRole, Role toRole){
        Account account = accountRepository.findByScrapYardYardIdAndRole(yardId, fromRole).getFirst();
        account.setRole(toRole);
        accountRepository.save(account);
    }
}