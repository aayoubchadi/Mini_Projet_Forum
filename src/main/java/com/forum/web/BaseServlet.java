package com.forum.web;

import java.io.IOException;

import com.forum.model.User;
import com.forum.service.ForumStore;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public abstract class BaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected ForumStore store() {
        return ForumStore.getInstance();
    }

    protected User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userId = session.getAttribute("userId");
        if (!(userId instanceof Integer)) {
            return null;
        }
        return store().findUserById((Integer) userId);
    }

    protected User requireUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        return user;
    }
}
