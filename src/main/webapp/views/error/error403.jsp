<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/error_header.jspf">
	<c:param name="pageTitle" value="403 - Zugriff Verweigert" />
</c:import>

<div class="error-page-container">
	<h1 class="error-code" style="color: var(--warning-color);">403</h1>
	<h2>Zugriff Verweigert</h2>
	<p class="error-message-text">Ihre Zugriffsebene ist für die
		angeforderte Ressource nicht ausreichend. Das Sicherheitsprotokoll
		wurde aktiviert.</p>

	<!-- Interactive Security Console -->
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

<style>
.error-page-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	text-align: center;
	min-height: 70vh;
	padding: 2rem;
}

.error-code {
	font-size: 5rem;
	font-weight: 800;
	line-height: 1;
	margin: 0;
}

.error-page-container h2 {
	font-size: 1.75rem;
	border: none;
	margin: 0 0 1.5rem 0;
}

.error-message-text {
	max-width: 600px;
	font-size: 1.1rem;
	line-height: 1.6;
	margin-bottom: 2rem;
}

.security-console {
	width: 100%;
	max-width: 700px;
	background: #000;
	border-radius: 6px;
	border: 1px solid var(--warning-color);
	box-shadow: 0 0 15px rgba(255, 193, 7, 0.3);
	font-family: monospace, monospace;
}

.console-header {
	background: #333;
	padding: 8px 12px;
	border-top-left-radius: 5px;
	border-top-right-radius: 5px;
	color: var(--warning-color);
	font-weight: bold;
	text-align: left;
}

#console-body {
	padding: 1rem;
	height: 250px;
	overflow-y: auto;
	text-align: left;
	white-space: pre-wrap;
	word-break: break-all;
}

#console-body .ok {
	color: var(--success-color);
}

#console-body .fail {
	color: var(--danger-color);
}

#console-body .info {
	color: var(--info-color);
}

#console-body .warn {
	color: var(--warning-color);
}

.cursor {
	display: inline-block;
	width: 0.6em;
	background-color: var(--warning-color);
	animation: blink 1s step-end infinite;
}

@
keyframes blink { 50% {
	background-color: transparent;
}
}
</style>

<c:import url="/WEB-INF/jspf/error_footer.jspf" />
<script>
document.addEventListener('DOMContentLoaded', () => {
    const consoleBody = document.getElementById('console-body');
    const redirectContainer = document.getElementById('redirect-container');
    const username = "${fn:escapeXml(sessionScope.user.username)}";
    const role = "${fn:escapeXml(sessionScope.user.roleName)}";
    const requestUri = "${fn:escapeXml(pageContext.errorData.requestURI)}";

    let currentLine;

    async function type(text, delay = 25) {
        for (const char of text) {
            currentLine.innerHTML += char;
            consoleBody.scrollTop = consoleBody.scrollHeight;
            await new Promise(resolve => setTimeout(resolve, delay));
        }
        currentLine.innerHTML = currentLine.innerHTML.replace('<span class="cursor"></span>', ''); 
        consoleBody.innerHTML += '\n'; // New line
    }
    
    async function addLine(text, className = '') {
        currentLine = document.createElement('span');
        if(className) currentLine.className = className;
        consoleBody.appendChild(currentLine);
        await type(text + ' <span class="cursor"></span>');
    }

    async function runSequence() {
        await addLine('[INFO] Eingehender Request erkannt...', 'info');
        await new Promise(resolve => setTimeout(resolve, 300));
        await addLine(`[INFO] Ziel-Ressource: ${requestUri}`, 'info');
        await new Promise(resolve => setTimeout(resolve, 400));
        await addLine('[INFO] Starte Identitäts-Scan...', 'info');
        await new Promise(resolve => setTimeout(resolve, 500));

        // CORRECTION: Added the 'ok' class to make this line green.
        await addLine(`[OK]   ... Subjekt identifiziert: "${username}"`, 'ok');

        await new Promise(resolve => setTimeout(resolve, 200));
        await addLine(`[OK]   ... Zugehörigkeit/Rolle: [${role}]`, 'ok');
        await new Promise(resolve => setTimeout(resolve, 400));
        await addLine('[WARN] Prüfe Berechtigungs-Matrix für Ziel-Ressource...', 'warn');
        await new Promise(resolve => setTimeout(resolve, 800));
        await addLine('[FAIL] >>> ZUGRIFF VERWEIGERT <<<', 'fail');
        await new Promise(resolve => setTimeout(resolve, 200));
        await addLine('[FAIL] >>> Erforderliche Berechtigung nicht im Token des Subjekts gefunden.', 'fail');
        await new Promise(resolve => setTimeout(resolve, 500));
        await addLine('[INFO] Aktion protokolliert. Sicherheits-Subsystem wird heruntergefahren.', 'info');
        
        currentLine = document.createElement('span');
        consoleBody.appendChild(currentLine);
        currentLine.innerHTML = '> <span class="cursor"></span>';

        redirectContainer.style.opacity = '1';

        setTimeout(() => {
            window.location.href = "${pageContext.request.contextPath}/home";
        }, 5000);
    }

    runSequence();
});
</script>