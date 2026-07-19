package com.scrapDetection.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String resetToken);
}
