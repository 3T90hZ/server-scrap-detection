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
import com.scrapDetection.service.*;
import com.scrapDetection.util.Normalize;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    private final Normalize normalize;
    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    private static final int TOKEN_EXPIRY_MINUTES = 60;

    @Override
    public AuthResponseDTO registerCustomer(CreateAccountRequestDTO request, Long yardId) {
        request.setEmail(normalize.normalizeEmailAndPhoneNumber(request.getEmail()));
        request.setPhoneNumbers(normalize.normalizeEmailAndPhoneNumber(request.getPhoneNumbers()));
        validateUniqueFields(request.getPhoneNumbers(), request.getEmail());

        Account account = accountMapper.toEntity(request);
        account.setRole(Role.CUSTOMER);
        account.setStatus(AccountStatus.ACTIVE);
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
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", "số điện thoại", request.getPhoneNumbers()));

        if(account.getScrapYard() != null && account.getRole() != Role.CUSTOMER){
            if( !YardStatus.ACTIVE.equals(account.getScrapYard().getStatus())){
                throw new InvalidRequestException("Vựa của bạn đang bị khoá hoặc chưa được duyệt");
            }
        }
        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new InvalidRequestException("Số điện thoại hoặc mật khẩu không đúng!");
        }
        if(AccountStatus.INACTIVE.equals(account.getStatus())){
            throw new InvalidRequestException("Tài khoản đã bị khoá!");
        }

        String token = jwtService.generateToken(account);
        sessionService.createSession(account, token);

        return accountMapper.toAuthResponse(account, token);
    }


    @Override
    public AuthResponseDTO createStaff(CreateAccountRequestDTO request) {
        request.setEmail(normalize.normalizeEmailAndPhoneNumber(request.getEmail()));
        request.setPhoneNumbers(normalize.normalizeEmailAndPhoneNumber(request.getPhoneNumbers()));
        validateUniqueFields(request.getPhoneNumbers(), request.getEmail());

        Account account = accountMapper.toEntity(request);
        account.setRole(Role.STAFF);
        account.setStatus(AccountStatus.ACTIVE);
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        Account currentUser = currentUserService.getCurrentUser();
        if (currentUser.getScrapYard() == null) {
            throw new InvalidRequestException("Chủ vựa phải thuộc về một vựa");
        }
        account.setScrapYard(currentUser.getScrapYard());

        Account saved = accountRepository.save(account);
        return accountMapper.toAuthResponse(saved, null);
    }

    @Override
    public void addStaff(String phoneNumber){
        Account currentUser = currentUserService.getCurrentUser();
        if(currentUser.getScrapYard() == null
                || currentUser.getScrapYard().getStatus().equals(YardStatus.INACTIVE)
                || !currentUser.getRole().equals(Role.YARD_OWNER)) {
            throw new InvalidRequestException("Không có quyền thêm tài khoản này làm nhân viên!");
        }
        Account staff = accountRepository.findByPhoneNumbers(phoneNumber).orElse(null);
        if(staff == null || staff.getRole() != Role.CUSTOMER || staff.getScrapYard() != null) {
            throw new InvalidRequestException("Không thể thêm tài khoản này làm nhân viên!");
        }

        notificationService.createInviteNotification(staff.getAccountId(), currentUser.getAccountId());
    }

    @Override
    public AuthResponseDTO updateAccount(Long accountId, AccountUpdateRequestDTO request) {
        Account existing = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", accountId));

        request.setEmail(normalize.normalizeEmailAndPhoneNumber(request.getEmail()));
        request.setPhoneNumbers(normalize.normalizeEmailAndPhoneNumber(request.getPhoneNumbers()));
        if (request.getPhoneNumbers() != null &&
                !request.getPhoneNumbers().equals(existing.getPhoneNumbers()) &&
                accountRepository.existsByPhoneNumbers(request.getPhoneNumbers())) {

            throw new ResourceAlreadyExistsException("Tài khoản", "số điện thoại", request.getPhoneNumbers());
        }

        if (request.getEmail() != null &&
                !request.getEmail().equals(existing.getEmail()) &&
                accountRepository.existsByEmail(request.getEmail())) {

            throw new ResourceAlreadyExistsException("Tài khoản", "số điện thoại", request.getPhoneNumbers());
        }
        if(request.getPassword()!=null){
            request.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        accountMapper.updateEntityFromDTO(request, existing);
        Account updated = accountRepository.save(existing);

        return accountMapper.toAuthResponse(updated, null);
    }

    @Override
    public void requestPasswordReset(PasswordResetRequestDTO request) {
        String value =  (request.getEmailOrPhone());

        if (isEmail(value)) {
            // Email-based password reset
            Account account = accountRepository.findByEmail(value)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + value));

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
        } else if (isPhoneNumber(value)) {
            throw new InvalidRequestException("Chức năng gửi mã OTP bằng số điện thoại hiện tại không khả dụng!");
        } else {
            throw new InvalidRequestException("Sai định dạng số điện thoại hoặc email");
        }
    }

    private boolean isEmail(String value) {
        return value.contains("@") && value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isPhoneNumber(String value) {
        // Adjust the regex to your accepted formats (E.164, local, etc.)
        String digitsOnly = value.replaceAll("[^0-9+]", "");
        return digitsOnly.matches("^\\+?[0-9]{8,15}$");
    }

    @Override
    public void resetPassword(PasswordResetConfirmDTO request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getResetToken())
                .orElseThrow(() -> new InvalidTokenException("Yêu cầu đã hết hạn hoặc không hợp lệ"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new InvalidTokenException("Yêu cầu đã hết hạn, vui lòng tạo yêu cầu mới!");
        }

        Account account = resetToken.getAccount();

        // Update password
        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        // Invalidate old sessions (optional but recommended)
        sessionService.invalidateAllSessions(account.getAccountId());

        // Clean up token
        tokenRepository.delete(resetToken);
    }

    @Override
    public List<AccountInfoResponseDTO> getAllStaffByYardOwner() {
        Account current = currentUserService.getCurrentUser();
        if (current.getScrapYard() == null) {
            throw new InvalidRequestException("Bạn không thuộc một vựa nào!");
        }
        List<Account> accounts = accountRepository.findByScrapYardYardIdAndRole(current.getScrapYard().getYardId(), Role.STAFF);
        return accountMapper.toAccountInfoResponseList(accounts);
    }

    @Override
    public AccountInfoResponseDTO updateAccountStatus(Long currentAccountId,ChangeAccountStatusRequestDTO dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", dto.getAccountId()));
        Account currentAccount = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", currentAccountId));
        if(currentAccount.getRole() != Role.ADMIN && !dto.getAccountId().equals(currentAccountId)){
            throw new InvalidRequestException("Không có quyền!");
        }
        if(currentAccount.getRole() == Role.ADMIN && dto.getAccountId().equals(currentAccountId)){
            throw new InvalidRequestException("Không có quyền!");
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

    @Override
    public void changeRole(Long yardId, Role fromRole, Role toRole){
        Account account = accountRepository.findByScrapYardYardIdAndRole(yardId, fromRole).getFirst();
        account.setRole(toRole);
        accountRepository.save(account);
    }

    @Override
    public void leaveYard(Long accountId) {
        Account currentAccount = currentUserService.getCurrentUser();
        Account leavingAccount = accountRepository.
                findById(accountId).
                orElseThrow(() -> new ResourceNotFoundException("Tài khoản", accountId));
        // Staff leave yard
        if(currentAccount.getAccountId().equals(accountId) && currentAccount.getRole() == Role.STAFF){
            leavingAccount.setRole(Role.CUSTOMER);
            leavingAccount.setScrapYard(null);
        }
        // Yard owner remove staff
        else if(!currentAccount.getAccountId().equals(accountId)
                && currentAccount.getRole() == Role.YARD_OWNER
                && leavingAccount.getRole().equals(Role.STAFF)
                && currentAccount.getScrapYard() != null
                && leavingAccount.getScrapYard() != null
                && leavingAccount.getScrapYard().getYardId().equals(currentAccount.getScrapYard().getYardId())){
            leavingAccount.setRole(Role.CUSTOMER);
            leavingAccount.setScrapYard(null);
        }
        else{
            throw new InvalidRequestException("Không có quyền!");
        }
        accountRepository.save(leavingAccount);
    }

    @Override
    public AccountInfoResponseDTO findAccountByPhoneNumber(String phoneNumber){
        Account account = accountRepository.findByPhoneNumbers(phoneNumber).orElse(null);
        if(account == null){
            return AccountInfoResponseDTO.builder()
                    .accountId(null)
                    .accountName("Khách vãng lai")
                    .phoneNumbers(null)
                    .status(null)
                    .build();
        }
        return accountMapper.toAccountInfoResponse(account);
    }

    @Override
    public AuthResponseDTO getMyInfo(){
        return accountMapper.toAuthResponse(currentUserService.getCurrentUser(), null);
    }
    // ==================== Helper Methods ====================

    private void validateUniqueFields(String phone, String email) {
        if (accountRepository.existsByPhoneNumbers(phone)) {
            throw new ResourceAlreadyExistsException("Tài khoản", "số điện thoại", phone);
        }
        if (email != null && accountRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Tài khoản", "email", email);
        }
    }
}