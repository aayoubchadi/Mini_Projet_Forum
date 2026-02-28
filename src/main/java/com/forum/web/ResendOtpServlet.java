package com.forum.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ResendOtpServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        boolean sent = store().sendVerificationCode(email);

        request.setAttribute("email", email);
        request.setAttribute("otpSent", sent);
        request.setAttribute("info", sent ? "verify.resent" : "verify.send.failed");
        request.getRequestDispatcher("/WEB-INF/views/register_success.jsp").forward(request, response);
    }
}
