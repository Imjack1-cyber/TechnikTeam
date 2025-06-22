<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<jsp:useBean id="now" class="java.util.Date" />

<%--
  error404.jsp
  
  This is a custom, user-friendly "Page Not Found" page. It is displayed
  whenever a user navigates to a URL that does not map to any servlet or
  resource in the application. The mapping is configured in web.xml.
  It does not use the standard navigation for a cleaner error display.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="404 - Seite nicht gefunden" />
</c:import>

<header class="app-header">
	<a href="${pageContext.request.contextPath}/home" class="logo">Technik
		Team</a>
</header>

<main>
	<div class="error-container">
		<div class="error-icon">⚠️</div>
		<h1>Seite nicht gefunden</h1>
		<h2>Fehlercode 404</h2>

		<p>
			Die von dir aufgerufene Seite <strong><c:out
					value="${pageContext.errorData.requestURI}" /></strong> existiert leider
			nicht.
		</p>
		<p>Möglicherweise hast du dich vertippt oder der Link ist
			veraltet.</p>

		<a href="${pageContext.request.contextPath}/home" class="btn">Zurück
			zur Startseite</a>

		<div class="error-details">
			Fehlerzeitpunkt:
			<fmt:formatDate value="${now}" type="both" dateStyle="long"
				timeStyle="medium" />
		</div>
	</div>
</main>

<style>
/* Specific styles for the error page content */
.error-container {
	text-align: center;
	padding: 2rem;
	margin: 2rem auto;
	max-width: 650px;
	background-color: var(--surface-color);
	border-radius: 12px;
	border: 1px solid var(--border-color);
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.error-icon {
	font-size: 4rem;
	color: var(--primary-color);
	margin-bottom: 1rem;
}

.error-container h1 {
	font-size: 2.2rem;
	color: var(--text-color);
	margin-bottom: 0.25rem;
}

.error-container h2 {
	font-size: 1.2rem;
	color: var(--text-muted-color);
	font-weight: 500;
	margin-bottom: 2rem;
}

.error-container p {
	margin-bottom: 1rem;
	font-size: 1.1rem;
	line-height: 1.6;
}

.error-container .btn {
	margin-top: 1rem;
}

.error-details {
	margin-top: 2rem;
	font-size: 0.85rem;
	color: var(--text-muted-color);
	border-top: 1px solid var(--border-color);
	padding-top: 1rem;
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />