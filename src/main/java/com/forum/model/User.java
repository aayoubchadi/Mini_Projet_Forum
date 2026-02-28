package com.forum.model;

import java.time.LocalDateTime;

public class User {
    private final int id;
    private String fullName;
    private final String email;
    private final String passwordHash;
    private boolean verified;
    private String verificationToken;
    private String preferredLanguage;
    private String bio;
    private final LocalDateTime createdAt;

    public User(int id, String fullName, String email, String passwordHash, boolean verified, String verificationToken,
            String preferredLanguage, String bio, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.verified = verified;
        this.verificationToken = verificationToken;
        this.preferredLanguage = preferredLanguage;
        this.bio = bio;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
