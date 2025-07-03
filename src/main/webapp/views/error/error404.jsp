<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/error_header.jspf">
	<c:param name="pageTitle" value="404 - Seite nicht gefunden" />
</c:import>

<div class="error-page-container">
	<h1 class="error-code">404</h1>
	<h2>Ressource nicht gefunden</h2>
	<p class="error-message-text">
		Sie haben sich im Dateisystem verirrt. Die angeforderte Ressource
		wurde nicht gefunden. <br>Das Systemprotokoll unten zeigt weitere
		Details.
	</p>

	<div class="terminal-window">
		<div class="terminal-header">
			<div class="terminal-buttons">
				<span class="term-btn close"></span><span class="term-btn min"></span><span
					class="term-btn max"></span>
			</div>
			<span>bash -- technik-team</span>
		</div>
		<div id="terminal-body"></div>
	</div>

	<div id="home-link-container"
		style="margin-top: 1.5rem; opacity: 0; transition: opacity 0.5s;">
		<a href="${pageContext.request.contextPath}/home"
			class="btn btn-primary"> <i class="fas fa-home"></i> Zurück zur
			Startseite
		</a>
	</div>
</div>

<style>
.terminal-window {
	width: 100%;
	max-width: 650px;
	background: #000;
	border-radius: 8px;
	box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
	font-family: monospace, monospace;
}

.terminal-header {
	background: #333;
	padding: 8px;
	border-top-left-radius: 8px;
	border-top-right-radius: 8px;
	display: flex;
	align-items: center;
	color: #ccc;
	font-size: 0.9em;
}

.terminal-buttons {
	display: flex;
	gap: 6px;
	margin-right: 12px;
}

.term-btn {
	display: block;
	width: 12px;
	height: 12px;
	border-radius: 50%;
}

.term-btn.close {
	background: #ff5f56;
}

.term-btn.min {
	background: #ffbd2e;
}

.term-btn.max {
	background: #27c93f;
}

#terminal-body {
	padding: 1rem;
	height: 200px;
	overflow-y: auto;
	text-align: left;
}

#terminal-body p {
	margin: 0;
	white-space: pre-wrap;
	word-break: break-all;
}

#terminal-body .prompt {
	color: var(--success-color);
}

#terminal-body .command {
	color: #fff;
}

#terminal-body .error {
	color: var(--danger-color);
}

#terminal-body .info {
	color: var(--info-color);
}

#terminal-body .link {
	color: var(--success-color);
	text-decoration: underline;
	cursor: pointer;
}

#terminal-body .cursor {
	background-color: #fff;
}
</style>

<c:import url="/WEB-INF/jspf/error_footer.jspf" />
<script type="text/javascript" src="/js/error/error404.js"></script>