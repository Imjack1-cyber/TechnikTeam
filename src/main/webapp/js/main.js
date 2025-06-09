/**
 * Main JavaScript file for the TechnikTeam application.
 * Contains global logic for theme switching, user interactions, and notifications.
 */
document.addEventListener('DOMContentLoaded', () => {
    
    // --- 1. Theme Switcher Logic ---
	// Ersetzen Sie die alte themeSwitcher-Logik mit dieser Version
	const themeCheckbox = document.getElementById('theme-checkbox');

	function applyTheme(theme) {
	    document.documentElement.setAttribute('data-theme', theme);
	    if (theme === 'dark') {
	        themeCheckbox.checked = true;
	    } else {
	        themeCheckbox.checked = false;
	    }
	}

	// Apply saved theme on page load
	const savedTheme = localStorage.getItem('theme') || 'light';
	applyTheme(savedTheme);

	// Add event listener to toggle the theme
	themeCheckbox.addEventListener('change', () => {
	    if (themeCheckbox.checked) {
	        localStorage.setItem('theme', 'dark');
	        applyTheme('dark');
	    } else {
	        localStorage.setItem('theme', 'light');
	        applyTheme('light');
	    }
	});

    // --- 2. Logout Confirmation Logic ---
    const logoutLink = document.getElementById('logout-link');
    if (logoutLink) {
        logoutLink.addEventListener('click', (event) => {
            const userIsSure = confirm('Bist du sicher, dass du dich ausloggen möchtest?');
            if (!userIsSure) {
                event.preventDefault(); // Prevent the browser from following the link if the user clicks "Cancel"
            }
        });
    }

    // --- 3. Event Details Toggle Logic ---
    document.querySelectorAll('.btn-details').forEach(button => {
        button.addEventListener('click', () => {
            // Find the details container, which is assumed to be the next sibling element
            // of the button's parent container (.event-actions)
            const detailsContainer = button.closest('.event-actions').nextElementSibling;
            if (detailsContainer && detailsContainer.classList.contains('event-details')) {
                if (detailsContainer.style.display === 'none' || detailsContainer.style.display === '') {
                    detailsContainer.style.display = 'block';
                    button.textContent = 'Details ausblenden';
                } else {
                    detailsContainer.style.display = 'none';
                    button.textContent = 'Details anzeigen';
                }
            }
        });
    });

    // --- 4. Server-Sent Events (SSE) Notification Logic ---
    // Only attempt to connect if the user is logged in (i.e., the main navigation is present)
    const mainNav = document.querySelector('.main-nav');
    if (mainNav && window.EventSource) {
        console.log("User is logged in. Connecting to SSE notification stream...");
        const eventSource = new EventSource('notifications'); // Relative URL is fine here

        eventSource.onopen = () => {
            console.log("SSE connection established.");
        };

        eventSource.onmessage = (event) => {
            console.log("Received SSE message:", event.data);
            showBrowserNotification(event.data);
        };

        eventSource.onerror = (err) => {
            console.error("EventSource failed. Connection will be closed.", err);
            eventSource.close(); // Prevent the browser from endlessly trying to reconnect
        };
    }

    /**
     * Shows a native browser notification after requesting permission if necessary.
     * @param {string} message The message to display in the notification body.
     */
    function showBrowserNotification(message) {
        if (!("Notification" in window)) {
            console.warn("This browser does not support desktop notifications.");
            return;
        }
        
        if (Notification.permission === "granted") {
            new Notification("Technik Team Update", { body: message });
        } else if (Notification.permission !== "denied") {
            Notification.requestPermission().then((permission) => {
                if (permission === "granted") {
                    new Notification("Technik Team Update", { body: message });
                }
            });
        }
    }
	
	
	
});