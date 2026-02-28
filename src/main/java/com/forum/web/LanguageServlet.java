package com.forum.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LanguageServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String lang = request.getParameter("lang");
        String selected = "en".equalsIgnoreCase(lang) ? "en" : "fr";
        request.getSession(true).setAttribute("lang", selected);

        String referer = request.getHeader("Referer");
        if (referer == null || referer.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        response.sendRedirect(referer);
    }
}
