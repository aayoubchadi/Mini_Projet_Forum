package com.forum.model;

import java.time.LocalDateTime;

public class Comment {
    private final int id;
    private final int articleId;
    private final int authorId;
    private final String content;
    private final LocalDateTime createdAt;

    public Comment(int id, int articleId, int authorId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getArticleId() {
        return articleId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
