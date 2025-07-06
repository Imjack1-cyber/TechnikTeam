<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/error_header.jspf">
	<c:param name="pageTitle" value="400 - Fehlerhafte Anfrage" />
</c:import>

<body
	data-content-length="${pageContext.request.contentLength > 0 ? pageContext.request.contentLength : 'UNKNOWN'}">
	<div class="error-page-container">
		<h1 class="error-code" style="color: var(--info-color);">400</h1>
		<h2>Fehlerhafte Anfrage</h2>
		<p class="error-message-text">Der Server konnte die Anfrage
			aufgrund einer fehlerhaften Syntax nicht verstehen. Das
			Protokolldroiden-System analysiert die Übertragung.</p>

		<div class="protocol-droid-console">
			<div class="droid-eye"></div>
			<pre id="droid-output"></pre>
		</div>

		<div id="redirect-container"
			style="margin-top: 1.5rem; opacity: 0; transition: opacity 0.5s;">
			<a href="javascript:history.back()" class="btn btn-secondary"> <i
				class="fas fa-arrow-left"></i> Einen Schritt zurück
			</a>
		</div>
	</div>
</body>

<c:import url="/WEB-INF/jspf/error_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/error/error400.js"></script>