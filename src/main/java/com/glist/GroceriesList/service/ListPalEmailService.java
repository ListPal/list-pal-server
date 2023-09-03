package com.glist.GroceriesList.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Getter
@Service
@RequiredArgsConstructor
public class ListPalEmailService {
    private final JavaMailSender javaMailSender;
    public void sendForgotPasswordLink(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@listpalsecurity.com");
        message.setTo(to);
        message.setSubject("Reset Password");
        message.setText("Follow this link " + "https://listpal.katespracticespace.com/#/password-reset?token=your-unique-token-here" + " to reset your password.");
        javaMailSender.send(message);
    }
}
