package com.scrapDetection.service.impl;

import com.scrapDetection.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    @Value("${app.frontend.base-url:https://frontend-domain.com}")
    private String frontendBaseUrl;

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();

        String resetLink = frontendBaseUrl + "/reset-password?token=" + resetToken;

        message.setTo(to);
        message.setSubject("Reset Your Password");
        message.setText("Click the link to reset your password: " + resetLink +
                "\n\nThis link expires in 1 hour.");

        javaMailSender.send(message);
    }
}
