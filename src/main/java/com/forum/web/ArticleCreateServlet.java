package com.forum.web;

import java.io.IOException;

import com.forum.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ArticleCreateServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }

        store().createArticle(user.getId(), request.getParameter("title"), request.getParameter("content"));
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
