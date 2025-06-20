/**
 * Main JavaScript file for the TechnikTeam application.
 * Contains global logic for theme switching, user interactions, and notifications.
 */
document.addEventListener('DOMContentLoaded', () => {
    
    // --- 1. Theme Switcher Logic ---
    const themeCheckbox = document.getElementById('theme-checkbox');

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        if (themeCheckbox) {
            themeCheckbox.checked = (theme === 'dark');
        }
    }

    const savedTheme = localStorage.getItem('theme') || 'light';
    applyTheme(savedTheme);

    if (themeCheckbox) {
        themeCheckbox.addEventListener('change', () => {
            const newTheme = themeCheckbox.checked ? 'dark' : 'light';
            localStorage.setItem('theme', newTheme);
            applyTheme(newTheme);
        });
    }

    // --- 2. Logout Confirmation Logic ---
    const logoutLink = document.getElementById('logout-link');
    if (logoutLink) {
        logoutLink.addEventListener('click', (event) => {
            if (!confirm('Bist du sicher, dass du dich ausloggen möchtest?')) {
                event.preventDefault();
            }
        });
    }

    // --- 3. Event Details Toggle Logic ---
    document.querySelectorAll('.btn-details').forEach(button => {
        button.addEventListener('click', () => {
            const detailsContainer = button.closest('.event-actions').nextElementSibling;
            if (detailsContainer && detailsContainer.classList.contains('event-details')) {
                const isHidden = detailsContainer.style.display === 'none' || detailsContainer.style.display === '';
                detailsContainer.style.display = isHidden ? 'block' : 'none';
                button.textContent = isHidden ? 'Details ausblenden' : 'Details anzeigen';
            }
        });
    });

    // --- 4. Server-Sent Events (SSE) Notification Logic ---
    const mainNav = document.querySelector('.main-nav');
    if (mainNav && window.EventSource) {
        console.log("User is logged in. Connecting to SSE notification stream...");
        
        // Build a reliable, absolute URL using the context path from the body tag.
        const contextPath = document.body.dataset.contextPath || '';
        const eventSource = new EventSource(`${contextPath}/notifications`);

        eventSource.onopen = () => {
            console.log("SSE connection established.");
        };

        eventSource.onmessage = (event) => {
            console.log("Received SSE message:", event.data);
            showBrowserNotification(event.data);
        };

        eventSource.onerror = (err) => {
            console.error("EventSource failed. Connection will be closed.", err);
            eventSource.close();
        };
    }

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