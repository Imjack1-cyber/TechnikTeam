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

<script>
document.addEventListener('DOMContentLoaded', () => {
    const terminalBody = document.getElementById('terminal-body');
    const homeLinkContainer = document.getElementById('home-link-container');
    const requestUri = "${fn:escapeXml(pageContext.errorData.requestURI)}";

    async function type(text, element, delay = 50) {
        for (const char of text) {
            element.textContent += char;
            terminalBody.scrollTop = terminalBody.scrollHeight;
            await new Promise(resolve => setTimeout(resolve, delay));
        }
    }
    
    async function addLine(text, className, delay = 20) {
        const p = document.createElement('p');
        if (className) p.className = className;
        terminalBody.appendChild(p);
        await type(text, p, delay);
    }

    async function runSequence() {
        const p1 = document.createElement('p');
        terminalBody.appendChild(p1);

        const prompt1 = document.createElement('span');
        prompt1.className = 'prompt';
        await type('user@technik-team:~$ ', prompt1, 20);
        p1.appendChild(prompt1);

        const command1 = document.createElement('span');
        command1.className = 'command';
        await type('ls -l ' + requestUri, command1, 50);
        p1.appendChild(command1);

        await new Promise(resolve => setTimeout(resolve, 500));

        await addLine('ls: cannot access \'' + requestUri + '\': No such file or directory', 'error', 15);
        await new Promise(resolve => setTimeout(resolve, 800));

        await addLine('Tipp: Kehren Sie mit dem folgenden Befehl zur Startseite zurück:', 'info', 25);
        await new Promise(resolve => setTimeout(resolve, 300));
        
        const p_link = document.createElement('p');
        terminalBody.appendChild(p_link);
        
        const prompt2 = document.createElement('span');
        prompt2.className = 'prompt';
        await type('user@technik-team:~$ ', prompt2, 20);
        p_link.appendChild(prompt2);

        const homeLink = document.createElement('a');
        homeLink.href = "${pageContext.request.contextPath}/home";
        homeLink.className = 'link';
        p_link.appendChild(homeLink);
        await type('cd /home', homeLink, 80);
        
        const cursor = document.createElement('span');
        cursor.className = 'cursor';
        cursor.innerHTML = ' ';
        p_link.appendChild(cursor);

        homeLinkContainer.style.opacity = '1';
    }

    runSequence();
});
</script>