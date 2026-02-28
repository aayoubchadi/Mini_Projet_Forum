package com.forum.web;

import java.io.IOException;

import com.forum.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Object flash = request.getSession(true).getAttribute("flash");
        if (flash != null) {
            request.setAttribute("flash", flash);
            request.getSession().removeAttribute("flash");
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = store().authenticate(request.getParameter("email"), request.getParameter("password"));
        if (user == null) {
            request.setAttribute("error", "login.error");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        request.getSession(true).setAttribute("userId", user.getId());
        request.getSession().setAttribute("lang", user.getPreferredLanguage());
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
