package com.forum.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.forum.model.Article;
import com.forum.model.Comment;
import com.forum.model.User;

public final class ForumStore {
    private static final ForumStore INSTANCE = new ForumStore();
    private static final int OTP_EXPIRY_MINUTES = 10;

    private final OracleConnectionFactory connectionFactory = new OracleConnectionFactory();
    private final GmailOtpService otpService = new GmailOtpService();

    private ForumStore() {
        if (connectionFactory.isPostgres()) {
            initPostgresSchema();
        }
        seedAdminIfMissing();
    }

    public static ForumStore getInstance() {
        return INSTANCE;
    }

    public User registerUser(String fullName, String email, String password, String lang) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty() || password == null || password.length() < 4 || fullName == null
                || fullName.trim().isEmpty()) {
            System.err.println("[ForumStore] registerUser: validation failed");
            return null;
        }

        String normalizedLang = "en".equalsIgnoreCase(lang) ? "en" : "fr";
        String code = generateOtpCode();

        // Use a SINGLE connection for check + delete + insert to avoid pooling issues
        try (Connection conn = connectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            // Check if email already exists
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT id, verified FROM FORUM_USERS WHERE email = ?")) {
                check.setString(1, normalizedEmail);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt("verified") == 1) {
                            conn.rollback();
                            System.err.println("[ForumStore] registerUser: email already verified: " + normalizedEmail);
                            return null; // genuinely taken
                        }
                        // Delete unverified account on the same connection
                        try (PreparedStatement del = conn.prepareStatement(
                                "DELETE FROM FORUM_USERS WHERE id = ?")) {
                            del.setInt(1, rs.getInt("id"));
                            del.executeUpdate();
                            System.out.println("[ForumStore] Deleted stale unverified account for " + normalizedEmail);
                        }
                    }
                }
            }

            // Insert new user on the same connection
            String sql = "INSERT INTO FORUM_USERS (full_name, email, password_hash, verified, verification_code, "
                    + "code_expires_at, preferred_language, bio, created_at) "
                    + "VALUES (?, ?, ?, 0, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, fullName.trim());
                ps.setString(2, normalizedEmail);
                ps.setString(3, sha256(password));
                ps.setString(4, code);
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
                ps.setString(6, normalizedLang);
                ps.setString(7, connectionFactory.isPostgres() ? "" : " ");
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }

            conn.commit();
            return findUserByEmail(normalizedEmail);
        } catch (SQLException e) {
            System.err.println("[ForumStore] registerUser failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean sendVerificationCode(String email) {
        User user = findUserByEmail(email);
        if (user == null || user.isVerified()) {
            return false;
        }

        String code = generateOtpCode();
        String sql = "UPDATE FORUM_USERS SET verification_code = ?, code_expires_at = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
            ps.setInt(3, user.getId());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                return false;
            }
            otpService.sendOtpCode(user.getEmail(), user.getFullName(), code, OTP_EXPIRY_MINUTES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verifyUserCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty() || code == null || code.trim().isEmpty()) {
            return false;
        }

        String checkSql = "SELECT id, code_expires_at, verification_code, verified FROM FORUM_USERS WHERE email = ?";
        String updateSql = "UPDATE FORUM_USERS SET verified = 1, verification_code = NULL, code_expires_at = NULL WHERE id = ?";

        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement checkPs = conn.prepareStatement(checkSql);
                PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
            checkPs.setString(1, normalizedEmail);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                int userId = rs.getInt("id");
                boolean verified = rs.getInt("verified") == 1;
                String dbCode = rs.getString("verification_code");
                Timestamp expiresAt = rs.getTimestamp("code_expires_at");

                if (verified || dbCode == null || expiresAt == null) {
                    return false;
                }
                if (!dbCode.equals(code.trim())) {
                    return false;
                }
                if (expiresAt.toLocalDateTime().isBefore(LocalDateTime.now())) {
                    return false;
                }

                updatePs.setInt(1, userId);
                return updatePs.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public User authenticate(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        String sql = "SELECT id, full_name, email, password_hash, verified, preferred_language, bio, created_at "
                + "FROM FORUM_USERS WHERE email = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String dbPasswordHash = rs.getString("password_hash");
                boolean verified = rs.getInt("verified") == 1;
                if (!verified || !dbPasswordHash.equals(sha256(password == null ? "" : password))) {
                    return null;
                }
                return mapUserRow(rs, null);
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public User findUserById(int userId) {
        String sql = "SELECT id, full_name, email, password_hash, verified, verification_code, preferred_language, bio, created_at "
                + "FROM FORUM_USERS WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapUserRow(rs, rs.getString("verification_code"));
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public User findUserByEmail(String email) {
        String sql = "SELECT id, full_name, email, password_hash, verified, verification_code, preferred_language, bio, created_at "
                + "FROM FORUM_USERS WHERE email = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapUserRow(rs, rs.getString("verification_code"));
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public void updateProfile(int userId, String fullName, String bio, String lang) {
        String normalizedLang = "en".equalsIgnoreCase(lang) ? "en" : "fr";
        String sql = "UPDATE FORUM_USERS SET full_name = ?, bio = ?, preferred_language = ? WHERE id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName == null || fullName.trim().isEmpty() ? "User" : fullName.trim());
            ps.setString(2, bio == null || bio.trim().isEmpty()
                    ? (connectionFactory.isPostgres() ? "" : " ")
                    : bio.trim());
            ps.setString(3, normalizedLang);
            ps.setInt(4, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update profile", e);
        }
    }

    public Article createArticle(int userId, String title, String content) {
        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            return null;
        }

        String sql = "INSERT INTO FORUM_ARTICLES (author_id, title, content, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, title.trim());
            ps.setString(3, content.trim());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

            int id = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }
            return new Article(id, userId, title.trim(), content.trim(), LocalDateTime.now());
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean deleteArticle(int articleId, int requesterUserId) {
        String sql = "DELETE FROM FORUM_ARTICLES WHERE id = ? AND author_id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            ps.setInt(2, requesterUserId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Article> listArticles() {
        String sql = "SELECT id, author_id, title, content, created_at FROM FORUM_ARTICLES ORDER BY created_at DESC";
        List<Article> articles = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                articles.add(new Article(
                        rs.getInt("id"),
                        rs.getInt("author_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime()));
            }
            return articles;
        } catch (SQLException e) {
            return articles;
        }
    }

    public Comment addComment(int articleId, int authorId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        String sql = "INSERT INTO FORUM_COMMENTS (article_id, author_id, content, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, articleId);
            ps.setInt(2, authorId);
            ps.setString(3, content.trim());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

            int id = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }
            return new Comment(id, articleId, authorId, content.trim(), LocalDateTime.now());
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean deleteComment(int commentId, int requesterUserId) {
        String sql = "DELETE FROM FORUM_COMMENTS WHERE id = ? AND author_id = ?";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setInt(2, requesterUserId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Comment> listCommentsByArticle(int articleId) {
        String sql = "SELECT id, article_id, author_id, content, created_at FROM FORUM_COMMENTS "
                + "WHERE article_id = ? ORDER BY created_at ASC";
        List<Comment> comments = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.add(new Comment(
                            rs.getInt("id"),
                            rs.getInt("article_id"),
                            rs.getInt("author_id"),
                            rs.getString("content"),
                            rs.getTimestamp("created_at").toLocalDateTime()));
                }
            }
            return comments;
        } catch (SQLException e) {
            return comments;
        }
    }

    private void initPostgresSchema() {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS FORUM_USERS ("
                + "id SERIAL PRIMARY KEY,"
                + "full_name VARCHAR(120) NOT NULL,"
                + "email VARCHAR(190) NOT NULL UNIQUE,"
                + "password_hash VARCHAR(128) NOT NULL,"
                + "verified SMALLINT DEFAULT 0 NOT NULL,"
                + "verification_code VARCHAR(6),"
                + "code_expires_at TIMESTAMP,"
                + "preferred_language VARCHAR(2) DEFAULT 'fr' NOT NULL,"
                + "bio VARCHAR(1000) DEFAULT '' NOT NULL,"
                + "created_at TIMESTAMP DEFAULT NOW() NOT NULL)",
            "CREATE TABLE IF NOT EXISTS FORUM_ARTICLES ("
                + "id SERIAL PRIMARY KEY,"
                + "author_id INTEGER NOT NULL,"
                + "title VARCHAR(200) NOT NULL,"
                + "content TEXT NOT NULL,"
                + "created_at TIMESTAMP DEFAULT NOW() NOT NULL,"
                + "CONSTRAINT fk_articles_author FOREIGN KEY (author_id) REFERENCES FORUM_USERS(id))",
            "CREATE TABLE IF NOT EXISTS FORUM_COMMENTS ("
                + "id SERIAL PRIMARY KEY,"
                + "article_id INTEGER NOT NULL,"
                + "author_id INTEGER NOT NULL,"
                + "content VARCHAR(2000) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT NOW() NOT NULL,"
                + "CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES FORUM_ARTICLES(id) ON DELETE CASCADE,"
                + "CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES FORUM_USERS(id))"
        };
        try (Connection conn = connectionFactory.getConnection(); Statement st = conn.createStatement()) {
            for (String s : ddl) {
                st.execute(s);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialise PostgreSQL schema.", e);
        }
    }

    private void seedAdminIfMissing() {
        User admin = findUserByEmail("admin@forum.local");
        if (admin != null) {
            return;
        }

        String bio = connectionFactory.isPostgres() ? "" : " ";
        String sql = "INSERT INTO FORUM_USERS (full_name, email, password_hash, verified, preferred_language, bio, created_at) "
                + "VALUES (?, ?, ?, 1, ?, ?, ?)";
        try (Connection conn = connectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Admin");
            ps.setString(2, "admin@forum.local");
            ps.setString(3, sha256("admin123"));
            ps.setString(4, "fr");
            ps.setString(5, bio);
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

            User inserted = findUserByEmail("admin@forum.local");
            if (inserted != null && listArticles().isEmpty()) {
                createArticle(inserted.getId(), "Bienvenue", "Bienvenue sur le forum JEE du TP.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Database seed failed. Ensure the schema is created (see sql/ folder).", e);
        }
    }

    private User mapUserRow(ResultSet rs, String verificationCode) throws SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        return new User(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getInt("verified") == 1,
                verificationCode,
                rs.getString("preferred_language"),
                rs.getString("bio"),
                created == null ? LocalDateTime.now() : created.toLocalDateTime());
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((input == null ? "" : input).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte value : hash) {
                sb.append(String.format("%02x", value));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private String generateOtpCode() {
        int value = new Random().nextInt(900000) + 100000;
        return String.valueOf(value);
    }
}
