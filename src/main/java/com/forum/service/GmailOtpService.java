package com.forum.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
        String subject = "Forum verification code";
        String body = "Hello " + (fullName == null ? "" : fullName) + ",\n\n"
                + "Your forum verification code is: " + code + "\n"
                + "This code expires in " + expiryMinutes + " minutes.\n\n"
                + "If you did not request this account, ignore this email.";

        // Send in background thread so registration responds immediately
        Thread emailThread = new Thread(() -> {
            String brevoKey = System.getenv("BREVO_API_KEY");
            if (brevoKey != null && !brevoKey.trim().isEmpty()) {
                sendViaBrevo(brevoKey.trim(), toEmail, fullName, subject, body);
            } else {
                sendViaGmailSmtp(toEmail, subject, body);
            }
        });
        emailThread.setDaemon(true);
        emailThread.start();
    }

    private void sendViaBrevo(String apiKey, String toEmail, String toName, String subject, String textBody) {
        try {
            String from = config.get("mail.from", "noreply@forum.local");
            // Escape JSON strings
            String safeName = (toName == null ? "" : toName).replace("\\", "\\\\").replace("\"", "\\\"");
            String safeBody = textBody.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");

            String json = "{\"sender\":{\"email\":\"" + from + "\",\"name\":\"Forum JEE\"},"
                    + "\"to\":[{\"email\":\"" + toEmail + "\",\"name\":\"" + safeName + "\"}],"
                    + "\"subject\":\"" + subject + "\","
                    + "\"textContent\":\"" + safeBody + "\"}";

            HttpURLConnection conn = (HttpURLConnection) URI.create("https://api.brevo.com/v3/smtp/email").toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("content-type", "application/json");
            conn.setRequestProperty("api-key", apiKey);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status >= 200 && status < 300) {
                System.out.println("[BrevoEmail] Email sent to " + toEmail + " (HTTP " + status + ")");
            } else {
                String resp = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                System.err.println("[BrevoEmail] Failed to send to " + toEmail + " (HTTP " + status + "): " + resp);
            }
        } catch (IOException e) {
            System.err.println("[BrevoEmail] Exception sending to " + toEmail + ": " + e.getMessage());
        }
    }

    private void sendViaGmailSmtp(String toEmail, String subject, String textBody) {
        try {
            String username = config.getRequired("gmail.username");
            String appPassword = config.getRequired("gmail.appPassword");
            String from = config.get("mail.from", username);

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, appPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(textBody);
            Transport.send(message);
            System.out.println("[GmailSmtp] Email sent to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("[GmailSmtp] Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }
}
