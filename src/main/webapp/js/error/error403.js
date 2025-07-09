document.addEventListener('DOMContentLoaded', () => {
	const consoleBody = document.getElementById('console-body');
	const redirectContainer = document.getElementById('redirect-container');
	const username = document.body.dataset.username || 'unknown_user';
	const role = document.body.dataset.role || 'unknown_role';
	const requestUri = document.body.dataset.requestUri || '/';
	const contextPath = document.body.dataset.contextPath || '';

	let currentLine;

	async function type(text, delay = 25) {
		for (const char of text) {
			currentLine.innerHTML += char;
			consoleBody.scrollTop = consoleBody.scrollHeight;
			await new Promise(resolve => setTimeout(resolve, delay));
		}
		currentLine.innerHTML = currentLine.innerHTML.replace('<span class="cursor"></span>', '');
		consoleBody.innerHTML += '\n'; 
	}

	async function addLine(text, className = '') {
		currentLine = document.createElement('span');
		if (className) currentLine.className = className;
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
			window.location.href = `${contextPath}/home`;
		}, 5000);
	}

	runSequence();
});