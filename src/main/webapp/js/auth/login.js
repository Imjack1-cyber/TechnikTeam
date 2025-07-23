document.addEventListener('DOMContentLoaded', () => {
	const lockoutTimer = document.getElementById('lockout-timer');
	if (lockoutTimer) {
		const endTime = parseInt(lockoutTimer.dataset.endTime, 10);
		const lockoutLevel = parseInt(lockoutTimer.dataset.lockoutLevel, 10);

		const durations = [60, 120, 300, 600, 1800]; // in seconds
		const duration = durations[Math.min(lockoutLevel, durations.length - 1)];

		const intervalId = setInterval(() => {
			const now = Date.now();
			const remainingSeconds = Math.max(0, Math.round((endTime + (duration * 1000) - now) / 1000));

			if (remainingSeconds <= 0) {
				clearInterval(intervalId);
				window.location.reload(); // Reload the page to clear the lockout message
			} else {
				const minutes = Math.floor(remainingSeconds / 60);
				const seconds = remainingSeconds % 60;
				lockoutTimer.textContent = `Bitte versuchen Sie es in ${minutes} Minute(n) und ${seconds.toString().padStart(2, '0')} Sekunde(n) erneut.`;
			}
		}, 1000);
	}
});