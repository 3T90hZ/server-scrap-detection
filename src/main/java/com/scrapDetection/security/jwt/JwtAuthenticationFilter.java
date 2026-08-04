package com.scrapDetection.security.jwt;

import com.scrapDetection.entity.Account;
import com.scrapDetection.repository.AccountRepository;
import com.scrapDetection.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                String phoneNumber = jwtService.extractPhoneNumber(token);

                Account account = accountRepository.findByPhoneNumbers(phoneNumber)
                        .orElseThrow(() -> new RuntimeException("Account not found"));

                if (jwtService.isTokenValid(token, account) && sessionService.isSessionValid(token)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    account,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    System.out.println("JWT Filter - Token validation FAILED");
                }
            } catch (Exception ex) {
                System.out.println("JWT Filter - ERROR: " + ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            System.out.println("JWT Filter - No token found in request");
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
}