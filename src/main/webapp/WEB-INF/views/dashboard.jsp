<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${sessionScope.lang != null ? sessionScope.lang : 'fr'}"/>
<fmt:setBundle basename="messages"/>
<!DOCTYPE html> 
<html>
<head>
<meta charset="UTF-8">
<title><fmt:message key="nav.dashboard"/></title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body>
<div class="container">
  <div class="navbar">
    <a class="brand" href="${pageContext.request.contextPath}/"><fmt:message key="app.title"/></a>
    <div class="actions">
      <a class="btn btn-soft" href="${pageContext.request.contextPath}/profile"><fmt:message key="nav.profile"/></a>
      <a class="btn btn-soft" href="${pageContext.request.contextPath}/logout"><fmt:message key="nav.logout"/></a>
    </div>
  </div>

  <div class="card">
    <h2><fmt:message key="member.welcome"/>, <c:out value="${user.fullName}"/></h2>
    <p class="meta"><fmt:message key="member.only"/></p>
  </div>

  <div class="card">
    <h3><fmt:message key="article.section"/></h3>
    <form method="post" action="${pageContext.request.contextPath}/article/create">
      <label><fmt:message key="article.title"/></label>
      <input name="title" required>
      <label><fmt:message key="article.content"/></label>
      <textarea name="content" required></textarea>
      <div class="form-actions">
        <button class="btn-primary" type="submit"><fmt:message key="article.create"/></button>
      </div>
    </form>
  </div>

  <c:forEach items="${articles}" var="article">
    <div class="card">
      <div class="article-head">
        <div>
          <h3><c:out value="${article.title}"/></h3>
          <p class="meta"><span class="tag">#${article.id}</span> <c:out value="${authorNames[article.authorId]}"/></p>
        </div>

        <c:if test="${article.authorId == user.id}">
          <form class="inline" method="post" action="${pageContext.request.contextPath}/article/delete">
            <input type="hidden" name="articleId" value="${article.id}">
            <button class="btn-danger" type="submit"><fmt:message key="article.delete"/></button>
          </form>
        </c:if>
      </div>

      <div class="article-body"><c:out value="${article.content}"/></div>

      <h4><fmt:message key="comment.section"/></h4>
      <form method="post" action="${pageContext.request.contextPath}/comment/create">
        <input type="hidden" name="articleId" value="${article.id}">
        <textarea name="content" placeholder="<fmt:message key='comment.placeholder'/>" required></textarea>
        <div class="form-actions">
          <button class="btn-soft" type="submit"><fmt:message key="comment.add"/></button>
        </div>
      </form>

      <div class="comment-list">
        <c:forEach items="${commentsByArticle[article.id]}" var="comment">
          <div class="comment-item">
            <div class="comment-line">
              <div class="small">
                <strong><c:out value="${authorNames[comment.authorId]}"/>:</strong>
                <c:out value="${comment.content}"/>
              </div>
              <c:if test="${comment.authorId == user.id}">
                <form class="inline" method="post" action="${pageContext.request.contextPath}/comment/delete">
                  <input type="hidden" name="commentId" value="${comment.id}">
                  <button class="btn-danger" type="submit"><fmt:message key="comment.delete"/></button>
                </form>
              </c:if>
            </div>
          </div>
        </c:forEach>
      </div>
    </div>
  </c:forEach>
</div>
</body>
</html>
