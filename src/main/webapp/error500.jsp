<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<jsp:useBean id="now" class="java.util.Date" />

<%--
  error500.jsp
  
  This is a custom "Internal Server Error" page. It is displayed whenever an
  uncaught exception occurs during the processing of a request. The mapping
  is configured in web.xml. For developers, it includes a hidden HTML comment
  with basic debugging information (the full stack trace is available in the
  server logs).
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="500 - Interner Serverfehler" />
</c:import>

<header class="app-header">
	<a href="${pageContext.request.contextPath}/home" class="logo">Technik
		Team</a>
</header>

<main>
	<div class="error-container">
		<div class="error-icon" style="color: var(--danger-color);">⚙️</div>
		<h1>Interner Serverfehler</h1>
		<h2>Fehlercode 500</h2>

		<p>Es ist ein unerwarteter technischer Fehler aufgetreten.</p>
		<p>Unser Team wurde bereits benachrichtigt. Bitte versuche es
			später erneut oder wende dich an einen Administrator, wenn der Fehler
			weiterhin bestehen bleibt.</p>

		<a href="${pageContext.request.contextPath}/home" class="btn">Zurück
			zur Startseite</a>

		<div class="error-details">
			Fehlerzeitpunkt:
			<fmt:formatDate value="${now}" type="both" dateStyle="long"
				timeStyle="medium" />
		</div>

		<%-- 
          DEBUGGING INFO: This block is hidden from regular users via an HTML comment.
          It allows developers to see error details by viewing the page source in the browser.
          The full stack trace is always available in the server logs (e.g., catalina.out).
        --%>
		<!--
            Exception Details for Developers:
            Request URI:    ${pageContext.errorData.requestURI}
            Servlet Name:   ${pageContext.errorData.servletName}
            Exception Type: ${pageContext.exception}
            Exception Msg:  ${pageContext.exception.message}
            Stack Trace:
            <c:forEach var="trace" items="${pageContext.exception.stackTrace}">
                ${trace}
            </c:forEach>
        -->
	</div>
</main>

<style>
/* Reuse the same styles from the 404 page for consistency */
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
	color: var(--danger-color);
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