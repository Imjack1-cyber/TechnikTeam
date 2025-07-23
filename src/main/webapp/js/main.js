/**
 * Main JavaScript file for the TechnikTeam application.
 * This file is included on every page and handles global user interface logic.
 */
document.addEventListener('DOMContentLoaded', () => {

	const contextPath = document.body.dataset.contextPath || '';
	const currentPage = document.body.dataset.page || '';

	// --- GLOBAL PASSWORD VISIBILITY TOGGLE ---
	document.body.addEventListener('click', (event) => {
		const toggle = event.target.closest('.password-toggle-icon');
		if (toggle) {
			const wrapper = toggle.closest('.password-input-wrapper');
			const input = wrapper.querySelector('input[type="password"], input[type="text"]');
			const icon = toggle.querySelector('i');
			if (input) {
				const isPassword = input.getAttribute('type') === 'password';
				input.setAttribute('type', isPassword ? 'text' : 'password');
				icon.classList.toggle('fa-eye', !isPassword);
				icon.classList.toggle('fa-eye-slash', isPassword);
			}
		}
	});

	// --- MOBILE NAVIGATION ---
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

	// --- ACTIVE NAV LINK HIGHLIGHTING ---
	const currentPath = window.location.pathname;
	document.querySelectorAll('.sidebar-nav a').forEach(link => {
		const linkPath = link.getAttribute('href');
		if (linkPath === currentPath) {
			link.classList.add('active-nav-link');
		} else if (currentPath.startsWith(linkPath) && linkPath !== `${contextPath}/home`) {
			// Make parent links active, e.g., /admin/lehrgaenge for /admin/meetings
			link.classList.add('active-nav-link');
		}
	});


	// --- THEME SWITCHER (SYNCED) ---
	const themeSwitches = document.querySelectorAll('.theme-switcher input[type="checkbox"]');
	const currentTheme = document.documentElement.dataset.theme || localStorage.getItem('theme') || 'light';
	document.documentElement.setAttribute('data-theme', currentTheme);

	const updateSwitches = (isDark) => {
		themeSwitches.forEach(sw => {
			sw.checked = isDark;
		});
	};

	updateSwitches(currentTheme === 'dark');

	themeSwitches.forEach(sw => {
		sw.addEventListener('change', (event) => {
			const newTheme = event.target.checked ? 'dark' : 'light';
			document.documentElement.setAttribute('data-theme', newTheme);
			localStorage.setItem('theme', newTheme);
			updateSwitches(event.target.checked);

			// Persist to server
			const csrfToken = document.body.dataset.csrfToken;
			const formData = new URLSearchParams();
			formData.append('theme', newTheme);
			if (csrfToken) {
				formData.append('csrfToken', csrfToken);
			}

			fetch(`${contextPath}/api/user/preferences`, {
				method: 'POST',
				body: formData
			}).catch(err => console.error("Failed to sync theme with server:", err));
		});
	});

	// --- GLOBAL CONFIRMATION MODAL ---
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

	// --- GLOBAL EVENT DELEGATION FOR MODALS & CONFIRMATIONS ---
	document.body.addEventListener('click', (event) => {
		// Modal Open
		const modalOpenTrigger = event.target.closest('[data-modal-target]');
		if (modalOpenTrigger) {
			event.preventDefault();
			const modalId = modalOpenTrigger.dataset.modalTarget;
			const modal = document.getElementById(modalId);
			if (modal) {
				modal.classList.add('active');
			}
		}

		// Modal Close
		const modalCloseTrigger = event.target.closest('[data-modal-close]');
		if (modalCloseTrigger) {
			event.preventDefault();
			const modal = modalCloseTrigger.closest('.modal-overlay');
			if (modal) {
				modal.classList.remove('active');
			}
		}
	});

	document.body.addEventListener('submit', (event) => {
		const form = event.target;
		if (form.matches('.js-confirm-form')) {
			event.preventDefault();
			const message = form.dataset.confirmMessage || 'Sind Sie sicher?';
			showConfirmationModal(message, () => form.submit());
		}
	});

	// --- GLOBAL TOAST NOTIFICATIONS ---
	window.showToast = (message, type = 'success') => {
		const toast = document.createElement('div');
		toast.className = `toast toast-${type}`;
		toast.innerHTML = `<p>${message}</p>`;

		document.body.appendChild(toast);

		setTimeout(() => {
			toast.classList.add('show');
		}, 100);

		setTimeout(() => {
			toast.classList.remove('show');
			setTimeout(() => {
				toast.remove();
			}, 500);
		}, 5000);
	};

	// --- SERVER-SENT EVENTS (SSE) NOTIFICATIONS & UI UPDATES ---
	if (document.body.dataset.isLoggedIn === 'true' && window.EventSource && currentPage !== 'editor') {
		const eventSource = new EventSource(`${contextPath}/notifications`);
		eventSource.onopen = () => console.log("SSE connection established.");
		eventSource.onmessage = (event) => {
			try {
				const data = JSON.parse(event.data);
				console.log("SSE data received:", data);
				if (data.type === 'ui_update') {
					handleUIUpdate(data.payload);
				} else if (data.type === 'logout_notification') {
                    // Special handler for forced logout
                    showToast(data.payload.message, 'info');
                    setTimeout(() => {
                        window.location.href = `${contextPath}/logout`;
                    }, 4000);
                } else {
					showBrowserNotification(data.payload);
				}
			} catch (e) {
				console.log("SSE (plain text) message received:", event.data);
				showBrowserNotification({ message: event.data });
			}
		};
		eventSource.onerror = (err) => {
			console.error("SSE connection error.", err);
			eventSource.close();
		};
	}

	function handleUIUpdate(payload) {
        console.log("Handling UI update:", payload.updateType, payload.data);
        switch (payload.updateType) {
            case 'user_updated':
                updateUserInUI(payload.data);
                break;
            case 'user_deleted':
                deleteUserFromUI(payload.data.userId);
                break;
            case 'event_status_updated':
                updateEventStatusInUI(payload.data.eventId, payload.data.newStatus);
                break;
            case 'feedback_status_updated':
                updateFeedbackStatusInUI(payload.data.submissionId, payload.data.newStatus);
                break;
            case 'feedback_deleted':
                deleteFeedbackFromUI(payload.data.submissionId);
                break;
        }
    }

	function updateUserInUI(user) {
		const userElements = document.querySelectorAll(`[data-user-id="${user.id}"]`);
		userElements.forEach(el => {
			el.querySelector('[data-field="username"]').textContent = user.username;
			el.querySelector('[data-field="roleName"]').textContent = user.roleName;
		});
	}

	function deleteUserFromUI(userId) {
		const userElements = document.querySelectorAll(`[data-user-id="${userId}"]`);
		userElements.forEach(el => {
			el.style.transition = 'opacity 0.5s';
			el.style.opacity = '0';
			setTimeout(() => el.remove(), 500);
		});
	}
	
	function getStatusBadgeClass(status, prefix = 'status-') {
        const classMap = {
            'LAUFEND': 'warn',
            'ABGESCHLOSSEN': 'info', 'ABGESAGT': 'info',
            'GEPLANT': 'ok', 'KOMPLETT': 'ok',
            // Feedback statuses
            'NEW': 'info', 'VIEWED': 'info', 'PLANNED': 'warn',
            'COMPLETED': 'ok', 'REJECTED': 'danger'
        };
        return prefix + (classMap[status] || 'info');
    }

	function updateEventStatusInUI(eventId, newStatus) {
		const eventElements = document.querySelectorAll(`[data-event-id="${eventId}"]`);
		eventElements.forEach(element => {
			const badge = element.querySelector('.status-badge');
			if (badge) {
				badge.textContent = newStatus;
				badge.className = `status-badge ${getStatusBadgeClass(newStatus)}`;
			}
	
			const actionsContainer = element.querySelector('.event-status-actions');
			if (actionsContainer) {
				actionsContainer.innerHTML = '';
	
				const csrfToken = document.body.dataset.csrfToken || '';
				const eventNameElement = element.querySelector('a');
				const eventName = eventNameElement ? eventNameElement.textContent.trim() : 'dieses Event';
	
				if (newStatus === 'GEPLANT' || newStatus === 'KOMPLETT') {
					actionsContainer.innerHTML = `
						<form action="${contextPath}/admin/veranstaltungen" method="post" style="display: inline;" class="js-confirm-form" data-confirm-message="Event '${eventName}' wirklich starten? Der Chat wird aktiviert.">
							<input type="hidden" name="csrfToken" value="${csrfToken}">
							<input type="hidden" name="action" value="updateStatus">
							<input type="hidden" name="id" value="${eventId}">
							<input type="hidden" name="newStatus" value="LAUFEND">
							<button type="submit" class="btn btn-small btn-warning">Starten</button>
						</form>`;
				} else if (newStatus === 'LAUFEND') {
					actionsContainer.innerHTML = `
						<form action="${contextPath}/admin/veranstaltungen" method="post" style="display: inline;" class="js-confirm-form" data-confirm-message="Event '${eventName}' wirklich abschließen?">
							<input type="hidden" name="csrfToken" value="${csrfToken}">
							<input type="hidden" name="action" value="updateStatus">
							<input type="hidden" name="id" value="${eventId}">
							<input type="hidden" name="newStatus" value="ABGESCHLOSSEN">
							<button type="submit" class="btn btn-small" style="background-color: var(--text-muted-color);">Abschließen</button>
						</form>`;
				}
			}
		});
	}	

    function updateFeedbackStatusInUI(submissionId, newStatus) {
        const card = document.querySelector(`[data-submission-id="${submissionId}"]`);
        if (!card) return;

        const badge = card.querySelector('.status-badge');
        if (badge) {
            badge.textContent = newStatus;
            badge.className = `status-badge ${getStatusBadgeClass(newStatus)}`;
        }

        const statusSelect = card.querySelector('select[name="status"]');
        if (statusSelect) {
            statusSelect.value = newStatus;
        }

        const deleteForm = card.querySelector('.js-feedback-delete-form');
        if (newStatus === 'COMPLETED' || newStatus === 'REJECTED') {
            if (!deleteForm) { // Add delete form if it doesn't exist
                const actionsContainer = card.querySelector('.card-actions');
                const newForm = document.createElement('form');
                newForm.className = 'js-feedback-delete-form';
                newForm.action = `${contextPath}/admin/action/feedback?action=delete`;
                newForm.method = 'POST';
                newForm.innerHTML = `
                    <input type="hidden" name="csrfToken" value="${document.body.dataset.csrfToken}">
                    <input type="hidden" name="submissionId" value="${submissionId}">
                    <button type="submit" class="btn btn-small btn-danger-outline">Löschen</button>
                `;
                actionsContainer.appendChild(newForm);
                // Re-attach event listener for the new form
                 newForm.addEventListener('submit', (e) => {
                    e.preventDefault();
                    showConfirmationModal('Diesen Feedback-Eintrag wirklich endgültig löschen?', async () => {
                        const formData = new FormData(newForm);
                        const response = await fetch(newForm.action, { method: 'POST', body: formData });
                        const result = await response.json();
                        if (response.ok && result.success) {
                            showToast(result.message, 'success');
                        } else {
                            showToast(result.message || 'Eintrag konnte nicht gelöscht werden.', 'danger');
                        }
                    });
                });
            }
        } else {
            // Remove delete form if it exists and status is not COMPLETED/REJECTED
            deleteForm?.remove();
        }
    }

    function deleteFeedbackFromUI(submissionId) {
        const card = document.querySelector(`[data-submission-id="${submissionId}"]`);
        if (card) {
            card.style.transition = 'opacity 0.5s';
            card.style.opacity = '0';
            setTimeout(() => card.remove(), 500);
        }
    }

	function showBrowserNotification(payload) {
		const message = payload.message || 'Neue Benachrichtigung';
		const url = payload.url;

		if (!("Notification" in window)) {
			console.warn("This browser does not support desktop notifications.");
			return;
		}

		const showNotification = () => {
			const notification = new Notification("Technik Team Update", {
				body: message,
				icon: `${contextPath}/images/favicon.ico`
			});

			if (url) {
				notification.onclick = (event) => {
					event.preventDefault();
					window.open(contextPath + url, '_blank');
				};
			}
		};

		if (Notification.permission === "granted") {
			showNotification();
		} else if (Notification.permission !== "denied") {
			Notification.requestPermission().then((permission) => {
				if (permission === "granted") {
					showNotification();
				}
			});
		}
	}


	// --- GLOBAL MARKDOWN RENDERER ---
	window.renderMarkdown = (element) => {
		if (!element || typeof marked === 'undefined') return;
		// Use marked's sanitizer to prevent XSS
		const sanitizedHtml = marked.parse(element.textContent || '', { sanitize: true });
		element.innerHTML = sanitizedHtml;
	};

	// Auto-render elements with the .markdown-content class
	document.querySelectorAll('.markdown-content').forEach(window.renderMarkdown);

});