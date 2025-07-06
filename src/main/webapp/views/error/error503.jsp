<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/error_header.jspf">
	<c:param name="pageTitle" value="503 - Dienst nicht verfügbar" />
</c:import>

<body data-context-path="${pageContext.request.contextPath}">
	<div class="error-page-container">
		<h1 class="error-code" style="color: #ff8c00;">503</h1>
		<h2>Dienst Nicht Verfügbar</h2>
		<p class="error-message-text">Der Server ist zurzeit überlastet
			oder wird gewartet. Bitte versuchen Sie es in Kürze erneut. Das
			System versucht einen Neustart.</p>

		<div class="reboot-console">
			<pre id="reboot-output"></pre>
			<div class="progress-bar-container"
				style="margin-top: 1rem; background: #333;">
				<div id="reboot-progress" class="progress-bar"
					style="background: var(--success-color);"></div>
			</div>
		</div>
	</div>
</body>

<c:import url="/WEB-INF/jspf/error_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/error/error503.js"></script>