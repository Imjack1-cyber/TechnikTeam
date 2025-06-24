/**
 * Main JavaScript file for the TechnikTeam application.
 * This file is included on every page and handles global user interface logic.
 *
 * It contains functionality for:
 * 1. Mobile Navigation: Toggling the slide-in menu (hamburger menu).
 * 2. Active Navigation Link Highlighting: Marks the current page in the nav bar.
 * 3. Theme Switching: Handling light/dark mode persistence via localStorage.
 * 4. Custom Confirmation Modal: A stylable replacement for the browser's confirm().
 * 5. Server-Sent Events (SSE): Establishing a connection to receive real-time
 *    push notifications from the server and displaying them as browser notifications.
 */
document.addEventListener('DOMContentLoaded', () => {

	// --- 1. Mobile Navigation Toggle Logic ---
	const navToggle = document.querySelector('.mobile-nav-toggle');
	const mainContent = document.querySelector('main');
	const mainNav = document.querySelector('.main-nav');

	const toggleNavigation = (e) => {
		e.stopPropagation();
		document.body.classList.toggle('nav-open');
	};

	const closeNavigation = () => {
		if (document.body.classList.contains('nav-open')) {
			document.body.classList.remove('nav-open');
		}
	};

	if (navToggle) {
		navToggle.addEventListener('click', toggleNavigation);
	}
	if (mainContent) {
		mainContent.addEventListener('click', closeNavigation);
	}
	if (mainNav) {
		// Prevent clicks inside the nav from closing it
		mainNav.addEventListener('click', (e) => e.stopPropagation());
	}


	// --- 2. Active Navigation Link Highlighting ---
	const currentPath = window.location.pathname;
	const contextPath = document.body.dataset.contextPath || '';
	const navLinks = document.querySelectorAll('.main-nav a');
	
	let bestMatch = null;
    let maxMatchLength = 0;

	navLinks.forEach(link => {
		const linkPath = link.getAttribute('href').substring(contextPath.length);
		// Ensure we don't match the root path as a prefix for everything
        if (linkPath && currentPath.startsWith(linkPath)) {
            if (linkPath.length > maxMatchLength) {
                maxMatchLength = linkPath.length;
                bestMatch = link;
            }
        }
	});
	
	if(bestMatch) {
		bestMatch.classList.add('active-nav-link');
	} else {
		// Fallback for home page if no other match is found
		const homeLink = document.querySelector('.main-nav a[href$="/home"]');
		if (homeLink && (currentPath === `${contextPath}/` || currentPath === `${contextPath}/home`)) {
			homeLink.classList.add('active-nav-link');
		}
	}


	// --- 3. Theme Switcher Logic ---
	const themeCheckbox = document.getElementById('theme-checkbox');
	const applyTheme = (theme) => {
		document.documentElement.setAttribute('data-theme', theme);
		if (themeCheckbox) {
			themeCheckbox.checked = (theme === 'dark');
		}
	};
	const savedTheme = localStorage.getItem('theme');
	const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
	applyTheme(savedTheme || (prefersDark ? 'dark' : 'light'));
	if (themeCheckbox) {
		themeCheckbox.addEventListener('change', () => {
			const newTheme = themeCheckbox.checked ? 'dark' : 'light';
			localStorage.setItem('theme', newTheme);
			applyTheme(newTheme);
		});
	}


	// --- 4. Custom Confirmation Modal Logic (made globally accessible) ---
	const modalElement = document.getElementById('confirmation-modal');
	if (modalElement) {
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
			if (typeof onConfirmCallback === 'function') {
				onConfirmCallback();
			}
			closeConfirmModal();
		});

		cancelBtn.addEventListener('click', closeConfirmModal);
		modalElement.addEventListener('click', (e) => {
			if (e.target === modalElement) closeConfirmModal();
		});
		
		// Logout Confirmation Logic (uses the global modal)
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
	}


	// --- 5. Server-Sent Events (SSE) Notification Logic ---
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
});