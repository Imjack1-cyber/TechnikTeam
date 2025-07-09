/**
 * Main JavaScript file for the TechnikTeam application.
 * This file is included on every page and handles global user interface logic.
 */
document.addEventListener('DOMContentLoaded', () => {

	const contextPath = document.body.dataset.contextPath || '';

	const navToggle = document.querySelector('.mobile-nav-toggle');
	const pageOverlay = document.querySelector('.page-overlay');
	if (navToggle) {
		navToggle.addEventListener('click', (event) => {
			event.stopPropagation();
			document.body.classList.toggle('nav-open');
		});
	}
	if (pageOverlay) {
		pageOverlay.addEventListener('click', () => {
			document.body.classList.remove('nav-open');
		});
	}

	const currentPath = window.location.pathname;
	document.querySelectorAll('.sidebar-nav a').forEach(link => {
		if (link.getAttribute('href') === currentPath) {
			link.classList.add('active-nav-link');
		}
	});


	const themeSwitch = document.getElementById('theme-toggle');
	const currentTheme = localStorage.getItem('theme') || 'light';
	document.documentElement.setAttribute('data-theme', currentTheme);

	if (themeSwitch) {
		if (currentTheme === 'dark') {
			themeSwitch.checked = true;
		}
		themeSwitch.addEventListener('change', (event) => {
			const newTheme = event.target.checked ? 'dark' : 'light';
			document.documentElement.setAttribute('data-theme', newTheme);
			localStorage.setItem('theme', newTheme);
		});
	}

	const confirmationModalElement = document.createElement('div');
	confirmationModalElement.className = 'modal-overlay';
	confirmationModalElement.id = 'confirmation-modal';
	confirmationModalElement.innerHTML = `
        <div class="modal-content" style="max-width: 450px;">
            <h3 id="confirmation-title">Bestätigung erforderlich</h3>
            <p id="confirmation-message" style="margin: 1.5rem 0; font-size: 1.1rem;"></p>
            <div style="display: flex; justify-content: flex-end; gap: 1rem;">
                <button id="confirmation-btn-cancel" class="btn" style="background-color: var(--text-muted-color);">Abbrechen</button>
                <button id="confirmation-btn-confirm" class="btn btn-danger">Bestätigen</button>
            </div>
        </div>`;
	document.body.appendChild(confirmationModalElement);

	const messageElement = document.getElementById('confirmation-message');
	const confirmBtn = document.getElementById('confirmation-btn-confirm');
	const cancelBtn = document.getElementById('confirmation-btn-cancel');

	let onConfirmCallback = null;

	window.showConfirmationModal = (message, onConfirm) => {
		messageElement.textContent = message;
		onConfirmCallback = onConfirm;
		confirmationModalElement.classList.add('active');
	};

	const closeConfirmModal = () => {
		confirmationModalElement.classList.remove('active');
		onConfirmCallback = null;
	};

	confirmBtn.addEventListener('click', () => {
		if (typeof onConfirmCallback === 'function') onConfirmCallback();
		closeConfirmModal();
	});

	cancelBtn.addEventListener('click', closeConfirmModal);
	confirmationModalElement.addEventListener('click', (e) => {
		if (e.target === confirmationModalElement) closeConfirmModal();
	});

	const logoutLink = document.getElementById('logout-link');
	if (logoutLink) {
		logoutLink.addEventListener('click', (event) => {
			event.preventDefault();
			showConfirmationModal(
				'Möchten Sie sich wirklich ausloggen?',
				() => { window.location.href = logoutLink.href; }
			);
		});
	}

	if (document.body.dataset.isLoggedIn === 'true' && window.EventSource) {
		const eventSource = new EventSource(`${contextPath}/notifications`);
		eventSource.onopen = () => console.log("SSE connection established.");
		eventSource.onmessage = (event) => {
			try {
				const data = JSON.parse(event.data);
				console.log("SSE data received:", data);

				if (data.type === 'chat_update') {
					const chatUpdateEvent = new CustomEvent('sse_chat_update', { detail: data });
					document.dispatchEvent(chatUpdateEvent);
				} else {
					showBrowserNotification(data.message || JSON.stringify(data));
				}
			} catch (e) {
				console.log("SSE (plain text) message received:", event.data);
				showBrowserNotification(event.data);
			}
		};
		eventSource.onerror = (err) => {
			console.error("SSE connection error.", err);
			eventSource.close();
		};
	}

	function showBrowserNotification(message) {
		if (!("Notification" in window)) {
			console.warn("This browser does not support desktop notifications.");
			return;
		}
		if (Notification.permission === "granted") {
			new Notification("Technik Team Update", { body: message, icon: `${contextPath}/images/logo.png` });
		} else if (Notification.permission !== "denied") {
			Notification.requestPermission().then((permission) => {
				if (permission === "granted") {
					new Notification("Technik Team Update", { body: message, icon: `${contextPath}/images/logo.png` });
				}
			});
		}
	}
});