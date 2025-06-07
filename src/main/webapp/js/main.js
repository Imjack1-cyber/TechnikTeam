document.addEventListener('DOMContentLoaded', () => {
    
    // --- 1. Theme Switcher Logic ---
    const themeSwitcher = document.getElementById('theme-switcher');
    const currentTheme = localStorage.getItem('theme');

    // Apply saved theme on load
    if (currentTheme) {
        document.documentElement.setAttribute('data-theme', currentTheme);
    }

    if(themeSwitcher) {
        themeSwitcher.addEventListener('click', () => {
            let theme = document.documentElement.getAttribute('data-theme');
            if (theme === 'dark') {
                document.documentElement.setAttribute('data-theme', 'light');
                localStorage.setItem('theme', 'light');
            } else {
                document.documentElement.setAttribute('data-theme', 'dark');
                localStorage.setItem('theme', 'dark');
            }
        });
    }

    // --- 2. Logout Confirmation Logic ---
    const logoutLink = document.getElementById('logout-link');
    if (logoutLink) {
        logoutLink.addEventListener('click', (event) => {
            const userIsSure = confirm('Bist du sicher, dass du dich ausloggen möchtest?');
            if (!userIsSure) {
                event.preventDefault(); // Stop the link from being followed
            }
        });
    }

    // --- 3. Event Details Toggle Logic ---
    document.querySelectorAll('.btn-details').forEach(button => {
        button.addEventListener('click', () => {
            // Find the details container, assuming it's the next sibling element
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
    const isUserLoggedIn = document.querySelector('.main-nav') !== null;

    if (isUserLoggedIn && window.EventSource) {
        console.log("Client is logged in. Connecting to SSE notification stream...");
        const eventSource = new EventSource('notifications');

        eventSource.onopen = () => {
            console.log("SSE connection established.");
        };

        eventSource.onmessage = (event) => {
            console.log("Received SSE message:", event.data);
            showBrowserNotification(event.data);
        };

        eventSource.onerror = (err) => {
            console.error("EventSource failed. Will not reconnect.", err);
            eventSource.close(); // Prevent browser from endlessly trying to reconnect
        };
    }

    function showBrowserNotification(message) {
        if (!("Notification" in window)) {
            console.warn("This browser does not support desktop notification");
            return;
        }
        
        if (Notification.permission === "granted") {
            new Notification("Technik Team Update", { body: message, icon: "path/to/icon.png" });
        } else if (Notification.permission !== "denied") {
            Notification.requestPermission().then((permission) => {
                if (permission === "granted") {
                    new Notification("Technik Team Update", { body: message, icon: "path/to/icon.png" });
                }
            });
        }
    }
});