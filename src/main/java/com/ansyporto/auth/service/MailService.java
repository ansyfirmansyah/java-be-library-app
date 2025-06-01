package com.ansyporto.auth.service;

import com.ansyporto.auth.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final AppProperties properties;

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMailFrom());
        message.setTo(to);
        message.setSubject("Verifikasi Email Anda");
        message.setText("Klik link berikut untuk verifikasi: " + properties.getVerificationUrl() + "?token=" + token);

        mailSender.send(message);
    }
}
