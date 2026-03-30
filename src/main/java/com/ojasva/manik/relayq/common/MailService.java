package com.ojasva.manik.relayq.common;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendCreationMessage(String to, String tempPassword, String tenantName) {
        sendEmail(to, "Account Created on RelayQ under " + tenantName, "Your account password is: " + tempPassword + "\nPlease change it after logging in.");
    }

    public void sendOtp(String to, String otp) {
        sendEmail(to, "Your OTP", "Your OTP is: " + otp + "\nIt expires in 10 minutes.");
    }

    public void sendTempPassword(String to, String tempPassword) {
        sendEmail(to, "Your Temporary Password", "Your temporary password is: " + tempPassword + "\nPlease change it after logging in.");
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}