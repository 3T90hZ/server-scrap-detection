package com.scrapDetection.service.impl;

import com.scrapDetection.entity.Account;
import com.scrapDetection.exception.InvalidRequestException;
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {
    private final AccountRepository accountRepository;
    @Override
    public Account getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidRequestException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Account principalAccount) {
            return accountRepository.findById(principalAccount.getAccountId())
                    .orElseThrow(() -> new InvalidRequestException("User not authenticated"));
        }

        throw new InvalidRequestException("User not authenticated");
    }
}
