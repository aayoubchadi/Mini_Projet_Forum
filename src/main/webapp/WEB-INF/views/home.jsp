<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${sessionScope.lang != null ? sessionScope.lang : 'fr'}"/>
<fmt:setBundle basename="messages"/>
<!DOCTYPE html> 
<html>
<head>
<meta charset="UTF-8">
<title><fmt:message key="app.title"/></title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body>
<div class="container">
  <div class="navbar">
    <a class="brand" href="${pageContext.request.contextPath}/"><fmt:message key="app.title"/></a>
    <div class="actions">
      <form class="inline" method="post" action="${pageContext.request.contextPath}/lang">
        <select name="lang" onchange="this.form.submit()">
          <option value="fr" ${sessionScope.lang == 'fr' || empty sessionScope.lang ? 'selected' : ''}><fmt:message key="lang.fr"/></option>
          <option value="en" ${sessionScope.lang == 'en' ? 'selected' : ''}><fmt:message key="lang.en"/></option>
        </select>
      </form>
      <c:choose>
        <c:when test="${not empty user}">
          <a class="btn btn-soft" href="${pageContext.request.contextPath}/dashboard"><fmt:message key="nav.dashboard"/></a>
          <a class="btn btn-soft" href="${pageContext.request.contextPath}/logout"><fmt:message key="nav.logout"/></a>
        </c:when>
        <c:otherwise>
          <a class="btn btn-soft" href="${pageContext.request.contextPath}/login"><fmt:message key="nav.login"/></a>
          <a class="btn btn-primary" href="${pageContext.request.contextPath}/register"><fmt:message key="nav.register"/></a>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <div class="card card-hero">
    <h1><fmt:message key="hero.title"/></h1>
    <p class="meta"><fmt:message key="hero.subtitle"/></p>
  </div>

  <c:forEach items="${articles}" var="article">
    <div class="card">
      <div class="article-head">
        <div>
          <h3><c:out value="${article.title}"/></h3>
          <p class="meta"><span class="tag">#${article.id}</span> <c:out value="${authorNames[article.authorId]}"/></p>
        </div>
      </div>
      <div class="article-body"><c:out value="${article.content}"/></div>

      <h4><fmt:message key="comment.section"/></h4>
      <div class="comment-list">
        <c:forEach items="${commentsByArticle[article.id]}" var="comment">
          <div class="comment-item">
            <div class="comment-line">
              <div class="small"><strong><c:out value="${authorNames[comment.authorId]}"/>:</strong> <c:out value="${comment.content}"/></div>
            </div>
          </div>
        </c:forEach>
      </div>
    </div>
  </c:forEach>
</div>
</body>
</html>
