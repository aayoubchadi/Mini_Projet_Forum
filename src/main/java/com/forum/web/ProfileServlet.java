package com.forum.web;

import java.io.IOException;

import com.forum.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProfileServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }
        request.setAttribute("user", user);
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return;
        }

        String fullName = request.getParameter("fullName");
        String bio = request.getParameter("bio");
        String lang = request.getParameter("lang");

        store().updateProfile(user.getId(), fullName, bio, lang);
        request.getSession().setAttribute("lang", "en".equalsIgnoreCase(lang) ? "en" : "fr");
        response.sendRedirect(request.getContextPath() + "/profile");
    }
}
