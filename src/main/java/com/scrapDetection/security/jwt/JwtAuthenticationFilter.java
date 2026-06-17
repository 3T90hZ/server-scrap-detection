package com.scrapDetection.security.jwt;

import com.scrapDetection.entity.Account;
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                String phoneNumber = jwtService.extractPhoneNumber(token);

                // Validate token and session
                if (jwtService.isTokenValid(token, getAccountByPhone(phoneNumber)) &&
                        sessionService.isSessionValid(token)) {

                    Account account = accountRepository.findByPhoneNumbers(phoneNumber)
                            .orElseThrow(() -> new RuntimeException("Account not found"));

                    // Set authentication in SecurityContext
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    account,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception ex) {
                // Log exception if needed
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        final String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Account getAccountByPhone(String phoneNumber) {
        return accountRepository.findByPhoneNumbers(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Account not found for token validation"));
    }
}