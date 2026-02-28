package com.forum.model;

import java.time.LocalDateTime;

public class Article {
    private final int id;
    private final int authorId;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;

    public Article(int id, int authorId, String title, String content, LocalDateTime createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
