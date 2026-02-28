package com.forum.web;

import java.io.IOException;

import com.forum.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CommentCreateServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }

        int articleId = parseInt(request.getParameter("articleId"));
        store().addComment(articleId, user.getId(), request.getParameter("content"));
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
