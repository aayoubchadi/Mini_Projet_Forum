<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${sessionScope.lang != null ? sessionScope.lang : 'fr'}"/>
<fmt:setBundle basename="messages"/>
<!DOCTYPE html>
<html>
<head> 
<meta charset="UTF-8">
<title><fmt:message key="nav.profile"/></title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body>
<div class="container">
  <div class="navbar">
    <a class="brand" href="${pageContext.request.contextPath}/dashboard"><fmt:message key="nav.dashboard"/></a>
    <div class="actions">
      <a class="btn btn-soft" href="${pageContext.request.contextPath}/home"><fmt:message key="nav.home"/></a>
      <a class="btn btn-soft" href="${pageContext.request.contextPath}/logout"><fmt:message key="nav.logout"/></a>
    </div>
  </div>

  <div class="card card-centered">
    <h2><fmt:message key="profile.section"/></h2>
    <p class="meta muted"><fmt:message key="member.welcome"/>, <c:out value="${user.fullName}"/></p>
    <form method="post" action="${pageContext.request.contextPath}/profile">
      <label><fmt:message key="auth.fullName"/></label>
      <input name="fullName" value="${user.fullName}" required>

      <label><fmt:message key="auth.email"/></label>
      <input value="${user.email}" disabled>

      <label><fmt:message key="profile.bio"/></label>
      <textarea name="bio">${user.bio}</textarea>

      <label><fmt:message key="lang.label"/></label>
      <select name="lang">
        <option value="fr" ${user.preferredLanguage == 'fr' ? 'selected' : ''}><fmt:message key="lang.fr"/></option>
        <option value="en" ${user.preferredLanguage == 'en' ? 'selected' : ''}><fmt:message key="lang.en"/></option>
      </select>
      <div class="form-actions">
        <button class="btn-primary" type="submit"><fmt:message key="profile.save"/></button>
      </div>
    </form>
  </div>
</div>
</body>
</html>
