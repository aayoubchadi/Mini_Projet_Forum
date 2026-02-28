<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${sessionScope.lang != null ? sessionScope.lang : 'fr'}"/>
<fmt:setBundle basename="messages"/>
<!DOCTYPE html> 
<html>
<head>
<meta charset="UTF-8">
<title><fmt:message key="nav.register"/></title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body>
<div class="container">
  <div class="navbar">
    <a class="brand" href="${pageContext.request.contextPath}/"><fmt:message key="app.title"/></a>
    <a class="btn btn-soft" href="${pageContext.request.contextPath}/login"><fmt:message key="nav.login"/></a>
  </div>

  <div class="card card-centered">
    <h2><fmt:message key="nav.register"/></h2>
    <p class="meta muted"><fmt:message key="hero.subtitle"/></p>
    <c:if test="${not empty error}"><div class="alert alert-err"><fmt:message key="${error}"/></div></c:if>
    <form method="post" action="${pageContext.request.contextPath}/register">
      <label><fmt:message key="auth.fullName"/></label>
      <input type="text" name="fullName" required>
      <label><fmt:message key="auth.email"/></label>
      <input type="email" name="email" required>
      <label><fmt:message key="auth.password"/></label>
      <input type="password" name="password" minlength="4" required>

      <label><fmt:message key="lang.label"/></label>
      <select name="lang">
        <option value="fr"><fmt:message key="lang.fr"/></option>
        <option value="en"><fmt:message key="lang.en"/></option>
      </select>
      <div class="form-actions">
        <button class="btn-primary btn-block" type="submit"><fmt:message key="auth.register"/></button>
      </div>
    </form>
  </div>
</div>
</body>
</html>
