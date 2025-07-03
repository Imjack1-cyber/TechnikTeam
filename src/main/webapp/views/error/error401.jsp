<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/error_header.jspf">
	<c:param name="pageTitle" value="401 - Nicht autorisiert" />
</c:import>

<div class="error-page-container">
	<h1 class="error-code" style="color: var(--danger-color);">401</h1>
	<h2>Nicht Autorisiert</h2>
	<p class="error-message-text">Für den Zugriff auf diese Ressource
		ist eine Authentifizierung erforderlich. Ihre Anmeldeinformationen
		fehlen oder sind ungültig.</p>

	<div class="card-scanner">
		<div class="scanner-light" id="scanner-light"></div>
		<div class="card-slot">
			<div class="card-content">
				<p>STATUS:</p>
				<h3 id="scanner-status">BEREIT</h3>
				<div class="progress-bar-container"
					style="height: 10px; margin-top: 1rem;">
					<div id="scanner-progress" class="progress-bar"></div>
				</div>
			</div>
		</div>
	</div>

	<div id="redirect-container"
		style="margin-top: 1.5rem; opacity: 0; transition: opacity 0.5s;">
		<a href="${pageContext.request.contextPath}/login"
			class="btn btn-success"> <i class="fas fa-sign-in-alt"></i> Zur
			Anmeldeseite
		</a>
	</div>
</div>

<c:import url="/WEB-INF/jspf/error_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/error/error401.js"></script>