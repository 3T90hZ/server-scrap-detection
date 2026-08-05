package com.scrapDetection.service.impl;

import com.scrapDetection.dto.account.AccountUpdateRequestDTO;
import com.scrapDetection.dto.account.AuthResponseDTO;
import com.scrapDetection.entity.Account;
import com.scrapDetection.exception.ResourceAlreadyExistsException;
import com.scrapDetection.mapper.AccountMapper;
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.repository.PasswordResetTokenRepository;
import com.scrapDetection.repository.ScrapYardRepository;
import com.scrapDetection.security.jwt.JwtService;
import com.scrapDetection.service.EmailService;
import com.scrapDetection.service.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ScrapYardRepository scrapYardRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private SessionService sessionService;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void updateAccount_nameOnly_doesNotHashOrOverwritePassword() {
        Account existing = Account.builder()
                .accountId(1L)
                .accountName("Tên cũ")
                .email("owner@example.com")
                .phoneNumbers("0900000000")
                .passwordHash("existing-hash")
                .build();
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Tên mới")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.save(existing)).thenReturn(existing);
        when(accountMapper.toAuthResponse(existing, null)).thenReturn(AuthResponseDTO.builder().build());

        accountService.updateAccount(1L, request);

        verifyNoInteractions(passwordEncoder);
        verify(accountMapper).updateEntityFromDTO(request, existing);
        verify(accountRepository).save(existing);
    }

    @Test
    void updateAccount_duplicateEmail_throwsConflictBeforeSaving() {
        Account existing = Account.builder()
                .accountId(1L)
                .email("owner@example.com")
                .phoneNumbers("0900000000")
                .build();
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .email("used@example.com")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.existsByEmail("used@example.com")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> accountService.updateAccount(1L, request));

        verify(accountRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, accountMapper);
    }

    @Test
    void updateAccount_passwordOnly_hashesNewPasswordBeforeMapping() {
        Account existing = Account.builder()
                .accountId(1L)
                .email("owner@example.com")
                .phoneNumbers("0900000000")
                .build();
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .password("new-password")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(accountRepository.save(existing)).thenReturn(existing);
        when(accountMapper.toAuthResponse(existing, null)).thenReturn(AuthResponseDTO.builder().build());

        accountService.updateAccount(1L, request);

        ArgumentCaptor<AccountUpdateRequestDTO> requestCaptor = ArgumentCaptor.forClass(AccountUpdateRequestDTO.class);
        verify(accountMapper).updateEntityFromDTO(requestCaptor.capture(), eq(existing));
        verify(passwordEncoder).encode("new-password");
        org.junit.jupiter.api.Assertions.assertEquals("new-hash", requestCaptor.getValue().getPassword());
    }
}
