package com.forum.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class VerifyEmailServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/register");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String code = request.getParameter("code");

        boolean success = store().verifyUserCode(email, code);
        if (success) {
            request.getSession(true).setAttribute("flash", "verify.ok");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setAttribute("email", email);
        request.setAttribute("otpSent", true);
        request.setAttribute("info", "verify.tryAgain");
        request.setAttribute("error", "verify.ko");
        request.getRequestDispatcher("/WEB-INF/views/register_success.jsp").forward(request, response);
    }
}
