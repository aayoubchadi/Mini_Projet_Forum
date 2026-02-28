package com.forum.web;

import java.io.IOException;

import com.forum.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RegisterServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String lang = request.getParameter("lang");

        User user = store().registerUser(fullName, email, password, lang);
        if (user == null) {
            request.setAttribute("error", "register.error");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        boolean otpSent = store().sendVerificationCode(user.getEmail());
        request.setAttribute("email", user.getEmail());
        request.setAttribute("otpSent", otpSent);
        request.getRequestDispatcher("/WEB-INF/views/register_success.jsp").forward(request, response);
    }
}
