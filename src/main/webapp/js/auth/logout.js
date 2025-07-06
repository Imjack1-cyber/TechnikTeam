document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	setTimeout(function() {
		window.location.href = `${contextPath}/login`;
	}, 5000);
});