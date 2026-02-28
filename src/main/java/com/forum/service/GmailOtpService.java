package com.forum.service;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public final class GmailOtpService {
    private final AppConfig config = AppConfig.getInstance();

    public void sendOtpCode(String toEmail, String fullName, String code, int expiryMinutes) {
        String username = config.getRequired("gmail.username");
        String appPassword = config.getRequired("gmail.appPassword");
        String from = config.get("mail.from", username);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Forum verification code");
            String body = "Hello " + (fullName == null ? "" : fullName) + ",\n\n"
                    + "Your forum verification code is: " + code + "\n"
                    + "This code expires in " + expiryMinutes + " minutes.\n\n"
                    + "If you did not request this account, ignore this email.";
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send OTP email via Gmail SMTP", e);
        }
    }
}
