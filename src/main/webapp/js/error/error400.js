document.addEventListener('DOMContentLoaded', () => {
	const output = document.getElementById('droid-output');
	const redirectContainer = document.getElementById('redirect-container');
	const contentLength = document.body.dataset.contentLength || 'UNKNOWN';

	const lines = [
		'INITIATING DATASTREAM ANALYSIS...',
		`PACKET RECEIVED. SIZE: ${contentLength} BYTES.`,
		'PARSING HEADER... [OK]',
		'ANALYSING PAYLOAD...',
		'    > SCANNING FOR SYNTAX VIOLATIONS...',
		'    > ERROR! UNEXPECTED TOKEN OR MALFORMED PARAMETER DETECTED.',
		'    > CORRUPTION LEVEL: MODERATE.',
		'CONCLUSION: ANFRAGE NICHT VERARBEITBAR. DATENINTEGRITÄT KOMPROMITTIERT.',
		'EMPFEHLUNG: ZURÜCKKEHREN UND ANFRAGE NEU FORMULIEREN.',
		''
	];

	let lineIndex = 0;

	function printLine() {
		if (lineIndex < lines.length) {
			const p = document.createElement('p');
			p.textContent = lines[lineIndex];
			output.appendChild(p);
			output.scrollTop = output.scrollHeight;
			lineIndex++;
			setTimeout(printLine, Math.random() * 200 + 50);
		} else {
			redirectContainer.style.opacity = '1';
		}
	}

	printLine();
});