package com.scrapDetection.service.impl;

import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Session;
import com.scrapDetection.repository.SessionRepository;
import com.scrapDetection.security.jwt.JwtService;
import com.scrapDetection.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final JwtService jwtService;

    @Override
    public Session createSession(Account account, String jwtToken) {
        String tokenHash = jwtService.getTokenHash(jwtToken);
        // Invalidate all previous sessions for this user (single device login policy)
        invalidateAllSessions(account.getAccountId());

        Session session = new Session();
        session.setAccount(account);
        session.setJwtTokenHash(tokenHash);
        session.setExpiredAt(LocalDateTime.now().plusHours(24)); // 24 hours

        return sessionRepository.save(session);
    }

    @Override
    public void invalidateSession(String jwtToken) {
        if (jwtToken == null) return;

        String tokenHash = jwtService.getTokenHash(jwtToken);
        sessionRepository.findByJwtTokenHash(tokenHash).ifPresent(sessionRepository::delete);
    }

    @Override
    public void invalidateAllSessions(Long accountId) {
        sessionRepository.deleteByAccountAccountId(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionValid(String jwtToken) {
        if (jwtToken == null) return false;

        String tokenHash = jwtService.getTokenHash(jwtToken);

        return sessionRepository.findByJwtTokenHash(tokenHash)
                .filter(session -> session.getExpiredAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Session> findByToken(String jwtToken) {
        String tokenHash = jwtService.getTokenHash(jwtToken);
        return sessionRepository.findByJwtTokenHash(tokenHash);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> getActiveSessions(Long accountId) {
        return sessionRepository.findByAccountAccountId(accountId).stream()
                .filter(session -> session.getExpiredAt().isAfter(LocalDateTime.now()))
                .toList();
    }

    @Override
    public void deleteExpiredSessions() {
        sessionRepository.deleteByExpiredAtBefore(LocalDateTime.now());
    }
}