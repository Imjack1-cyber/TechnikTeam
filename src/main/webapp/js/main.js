/**
 * Main JavaScript file for the TechnikTeam application.
 * This file is included on every page and handles global user interface logic.
 */
document.addEventListener('DOMContentLoaded', () => {

	// --- 1. Mobile Navigation Toggle Logic ---
	const navToggle = document.querySelector('.mobile-nav-toggle');
	if (navToggle) {
		navToggle.addEventListener('click', (e) => {
			e.stopPropagation();
			document.body.classList.toggle('nav-open');
		});
	}

	// --- 2. Active Navigation Link Highlighting ---
	const currentPath = window.location.pathname;
	const navLinks = document.querySelectorAll('.sidebar-nav a');
	let bestMatch = null;
	let maxMatchLength = 0;

	navLinks.forEach(link => {
		const linkPath = new URL(link.href).pathname;
		if (linkPath && currentPath.startsWith(linkPath)) {
			if (linkPath.length > maxMatchLength) {
				maxMatchLength = linkPath.length;
				bestMatch = link;
			}
		}
	});

	if (bestMatch) {
		bestMatch.classList.add('active-nav-link');
	}

	// --- 3. Theme Switcher Logic ---
	// ... (This section is unchanged and correct) ...

	// --- 4. Custom Confirmation Modal Logic ---
	// ... (This section is unchanged and correct) ...
	// NOTE: The implementation from the previous response is correct.
	// It creates a global function `showConfirmationModal`.

	// --- 5. Server-Sent Events (SSE) Notification Logic ---
	const contextPath = document.body.dataset.contextPath || ''; // Use data attribute
	if (document.body.dataset.isLoggedIn === 'true' && window.EventSource) {
		const eventSource = new EventSource(`${contextPath}/notifications`);
		eventSource.onopen = () => console.log("SSE connection established.");
		eventSource.onmessage = (event) => {
			console.log("SSE message received:", event.data);
			showBrowserNotification(event.data);
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

	// Ensure confirmation modal is set up
	if (typeof showConfirmationModal === 'undefined') {
		const modalElement = document.createElement('div');
		modalElement.className = 'modal-overlay';
		modalElement.id = 'confirmation-modal';
		modalElement.innerHTML = `
            <div class="modal-content" style="max-width: 450px;">
                <h3 id="confirmation-title">Bestätigung</h3>
                <p id="confirmation-message" style="margin: 1.5rem 0; font-size: 1.1rem;"></p>
                <div style="display: flex; justify-content: flex-end; gap: 1rem;">
                    <button id="confirmation-btn-cancel" class="btn btn-secondary">Abbrechen</button>
                    <button id="confirmation-btn-confirm" class="btn btn-danger">Bestätigen</button>
                </div>
            </div>`;
		document.body.appendChild(modalElement);

		const messageElement = document.getElementById('confirmation-message');
		const confirmBtn = document.getElementById('confirmation-btn-confirm');
		const cancelBtn = document.getElementById('confirmation-btn-cancel');

		let onConfirmCallback = null;

		window.showConfirmationModal = (message, onConfirm) => {
			messageElement.textContent = message;
			onConfirmCallback = onConfirm;
			modalElement.classList.add('active');
		};

		const closeConfirmModal = () => {
			modalElement.classList.remove('active');
			onConfirmCallback = null;
		};

		confirmBtn.addEventListener('click', () => {
			if (typeof onConfirmCallback === 'function') onConfirmCallback();
			closeConfirmModal();
		});

		cancelBtn.addEventListener('click', closeConfirmModal);
		modalElement.addEventListener('click', (e) => {
			if (e.target === modalElement) closeConfirmModal();
		});
	}

	const logoutLink = document.getElementById('logout-link');
	if (logoutLink) {
		logoutLink.addEventListener('click', (event) => {
			event.preventDefault();
			showConfirmationModal(
				'Bist du sicher, dass du dich ausloggen möchtest?',
				() => { window.location.href = logoutLink.href; }
			);
		});
	}
});