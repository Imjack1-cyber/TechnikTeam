<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/error_header.jspf">
	<c:param name="pageTitle" value="500 - Interner Fehler" />
</c:import>

<div class="error-page-container">
	<h1 class="error-code" style="color: var(--danger-color);">500</h1>
	<h2>Systemfehler im Hauptrechner</h2>
	<p class="error-message-text">Ein unerwarteter Fehler ist
		aufgetreten und unser System ist ins Stolpern geraten. Starten Sie die
		automatische Systemdiagnose, um den Fehler zu analysieren.</p>

	<div class="diagnostic-container">
		<button id="diagnostic-btn" class="btn btn-warning">
			<i class="fas fa-tasks"></i> Diagnose starten
		</button>
		<div id="diagnostic-output" class="diagnostic-console">
		</div>
	</div>

	<a href="${pageContext.request.contextPath}/home"
		class="btn btn-primary" style="margin-top: 1.5rem;"> <i
		class="fas fa-home"></i> Flucht zur Startseite
	</a>
</div>

<c:import url="/WEB-INF/jspf/error_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/error/error500.js"></script>