<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${sessionScope.lang != null ? sessionScope.lang : 'fr'}"/>
<fmt:setBundle basename="messages"/>
<!DOCTYPE html> 
<html>
<head>
<meta charset="UTF-8">
<title><fmt:message key="register.success"/></title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body>
<div class="container">
  <div class="navbar">
    <a class="brand" href="${pageContext.request.contextPath}/"><fmt:message key="app.title"/></a>
    <a class="btn btn-soft" href="${pageContext.request.contextPath}/login"><fmt:message key="nav.login"/></a>
  </div>

  <div class="card card-centered">
    <h2><fmt:message key="register.success"/></h2>
    <p class="meta muted"><fmt:message key="member.only"/></p>

    <c:if test="${not empty info}">
      <div class="alert alert-info"><fmt:message key="${info}"/></div>
    </c:if>

    <c:choose>
      <c:when test="${otpSent}">
        <div class="alert alert-ok"><fmt:message key="verify.sent"/></div>
      </c:when>
      <c:otherwise>
        <div class="alert alert-err"><fmt:message key="verify.send.failed"/></div>
      </c:otherwise>
    </c:choose>

    <c:if test="${not empty error}">
      <div class="alert alert-err"><fmt:message key="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/verify">
      <label><fmt:message key="auth.email"/></label>
      <input type="email" name="email" value="${email}" required>

      <label><fmt:message key="verify.code"/></label>
      <input type="text" name="code" maxlength="6" minlength="6" pattern="[0-9]{6}" required>
      <div class="form-actions">
        <button class="btn btn-ok" type="submit"><fmt:message key="verify.confirm"/></button>
      </div>
    </form>

    <form method="post" action="${pageContext.request.contextPath}/verify/resend">
      <input type="hidden" name="email" value="${email}">
      <button class="btn btn-ghost" type="submit"><fmt:message key="verify.resend"/></button>
    </form>
  </div>
</div>
</body>
</html>
