package com.scrapDetection.service;

import com.scrapDetection.entity.Account;
import com.scrapDetection.entity.Session;

import java.util.List;
import java.util.Optional;

public interface SessionService {

    /**
     * Create a new session for a user (used during login)
     */
    Session createSession(Account account, String jwtToken);

    /**
     * Invalidate a specific session (used during logout)
     */
    void invalidateSession(String jwtToken);

    /**
     * Invalidate all sessions of a user (logout from all devices)
     */
    void invalidateAllSessions(Long accountId);

    /**
     * Check if a session is valid
     */
    boolean isSessionValid(String jwtToken);

    /**
     * Find session by token hash
     */
    Optional<Session> findByToken(String jwtToken);

    /**
     * Get all active sessions for an account
     */
    List<Session> getActiveSessions(Long accountId);

    /**
     * Clean up expired sessions (can be scheduled)
     */
    void deleteExpiredSessions();
}