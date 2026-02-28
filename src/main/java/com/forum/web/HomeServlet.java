package com.forum.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.forum.model.Article;
import com.forum.model.Comment;
import com.forum.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HomeServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Article> articles = store().listArticles();
        Map<Integer, List<Comment>> commentsByArticle = new HashMap<>();
        Map<Integer, String> authorNames = new HashMap<>();
        for (Article article : articles) {
            commentsByArticle.put(article.getId(), store().listCommentsByArticle(article.getId()));
            User articleAuthor = store().findUserById(article.getAuthorId());
            authorNames.put(article.getAuthorId(), articleAuthor == null ? "Unknown" : articleAuthor.getFullName());
            for (Comment comment : commentsByArticle.get(article.getId())) {
                User commentAuthor = store().findUserById(comment.getAuthorId());
                authorNames.put(comment.getAuthorId(),
                        commentAuthor == null ? "Unknown" : commentAuthor.getFullName());
            }
        }

        request.setAttribute("articles", articles);
        request.setAttribute("commentsByArticle", commentsByArticle);
        request.setAttribute("authorNames", authorNames);
        request.setAttribute("user", currentUser(request));
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
