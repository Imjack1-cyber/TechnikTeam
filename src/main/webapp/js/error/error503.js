document.addEventListener('DOMContentLoaded', () => {
	const output = document.getElementById('reboot-output');
	const progress = document.getElementById('reboot-progress');
	const contextPath = document.body.dataset.contextPath || '';

	const steps = [
		{ text: 'System check initiated...', delay: 100, progress: 10 },
		{ text: 'Checking memory modules... [OK]', delay: 300, progress: 20 },
		{ text: 'Checking CPU cores... [OK]', delay: 300, progress: 30 },
		{ text: 'Pinging gateway... [NO RESPONSE]', delay: 1000, progress: 40 },
		{ text: 'Server overload detected. Reason: Too many requests.', delay: 500, progress: 50 },
		{ text: 'Flushing request queue...', delay: 1500, progress: 70 },
		{ text: 'Initializing reboot sequence...', delay: 500, progress: 80 },
		{ text: 'System will be back online shortly.', delay: 1000, progress: 100 },
		{ text: 'Redirecting to login page...', delay: 2000, progress: 100 }
	];

	let stepIndex = 0;

	function runSequence() {
		if (stepIndex >= steps.length) {
			window.location.href = `${contextPath}/login`;
			return;
		}

		const step = steps[stepIndex];
		const p = document.createElement('p');
		p.textContent = step.text;
		output.appendChild(p);
		output.scrollTop = output.scrollHeight;

		progress.style.width = `${step.progress}%`;

		stepIndex++;
		setTimeout(runSequence, step.delay);
	}

	runSequence();
});