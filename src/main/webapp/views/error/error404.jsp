<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/error_header.jspf">
	<c:param name="pageTitle" value="404 - Seite nicht gefunden" />
</c:import>

<body data-context-path="${pageContext.request.contextPath}"
	data-request-uri="${fn:escapeXml(pageContext.errorData.requestURI)}">

	<div class="error-page-container">
		<h1 class="error-code">404</h1>
		<h2>Ressource nicht gefunden</h2>
		<p class="error-message-text">
			Sie haben sich im Dateisystem verirrt. Die angeforderte Ressource
			wurde nicht gefunden. <br>Das Systemprotokoll unten zeigt
			weitere Details.
		</p>

		<div class="terminal-window">
			<div class="terminal-header">
				<div class="terminal-buttons">
					<span class="term-btn close"></span><span class="term-btn min"></span><span
						class="term-btn max"></span>
				</div>
				<span>bash -- technik-team</span>
			</div>
			<div id="terminal-body" class="terminal-body"></div>
		</div>

		<div id="home-link-container"
			style="margin-top: 1.5rem; opacity: 0; transition: opacity 0.5s;">
			<a href="${pageContext.request.contextPath}/home"
				class="btn btn-primary"> <i class="fas fa-home"></i> Zurück zur
				Startseite
			</a>
		</div>
	</div>
</body>

<c:import url="/WEB-INF/jspf/error_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/error/error404.js"></script>