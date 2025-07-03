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