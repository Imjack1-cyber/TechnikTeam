<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/error_header.jspf">
	<c:param name="pageTitle" value="403 - Zugriff Verweigert" />
</c:import>

<body data-context-path="${pageContext.request.contextPath}"
	data-username="${fn:escapeXml(sessionScope.user.username)}"
	data-role="${fn:escapeXml(sessionScope.user.roleName)}"
	data-request-uri="${fn:escapeXml(pageContext.errorData.requestURI)}">

	<div class="error-page-container">
		<h1 class="error-code" style="color: var(--warning-color);">403</h1>
		<h2>Zugriff Verweigert</h2>
		<p class="error-message-text">Ihre Zugriffsebene ist für die
			angeforderte Ressource nicht ausreichend. Das Sicherheitsprotokoll
			wurde aktiviert.</p>

		<div class="security-console">
			<div class="console-header">
				<span>SYSTEM SECURITY DAEMON - PROTOKOLL</span>
			</div>
			<pre id="console-body"></pre>
		</div>

		<div id="redirect-container"
			style="margin-top: 1.5rem; opacity: 0; transition: opacity 0.5s;">
			<a href="${pageContext.request.contextPath}/home"
				class="btn btn-primary"> <i class="fas fa-home"></i> Zurück zur
				Startseite
			</a>
		</div>
	</div>
</body>

<c:import url="/WEB-INF/jspf/error_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/error/error403.js"></script>