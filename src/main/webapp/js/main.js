/**
 * Main JavaScript file for the TechnikTeam application.
 * Contains global logic for:
 * 1. Mobile Navigation (Hamburger Menu)
 * 2. Theme Switching (Light/Dark Mode)
 * 3. User Interaction Confirmations (Logout)
 * 4. Server-Sent Events (SSE) for real-time notifications.
 */
document.addEventListener('DOMContentLoaded', () => {

    // --- 1. Mobile Navigation Toggle Logic ---
    const navToggle = document.querySelector('.mobile-nav-toggle');
    
    // Function to open/close the mobile navigation
    const toggleNavigation = () => {
        // This single class on the body controls the slide-in menu and overlay
        document.body.classList.toggle('nav-open');
    };

    // Add click listener to the hamburger button
    if (navToggle) {
        navToggle.addEventListener('click', toggleNavigation);
    }
    // Also add a listener to the main content area to close the nav if the user taps outside
    const mainContent = document.querySelector('main');
    if (mainContent) {
        mainContent.addEventListener('click', () => {
            if (document.body.classList.contains('nav-open')) {
                document.body.classList.remove('nav-open');
            }
        });
    }


    // --- 2. Theme Switcher Logic ---
    const themeCheckbox = document.getElementById('theme-checkbox');

    const applyTheme = (theme) => {
        document.documentElement.setAttribute('data-theme', theme);
        if (themeCheckbox) {
            themeCheckbox.checked = (theme === 'dark');
        }
    };

    const savedTheme = localStorage.getItem('theme') || 'light';
    applyTheme(savedTheme);

    if (themeCheckbox) {
        themeCheckbox.addEventListener('change', () => {
            const newTheme = themeCheckbox.checked ? 'dark' : 'light';
            localStorage.setItem('theme', newTheme);
            applyTheme(newTheme);
        });
    }


    // --- 3. Logout Confirmation Logic ---
    const logoutLink = document.getElementById('logout-link');
    if (logoutLink) {
        logoutLink.addEventListener('click', (event) => {
            if (!confirm('Bist du sicher, dass du dich ausloggen möchtest?')) {
                event.preventDefault();
            }
        });
    }


    // --- 4. Server-Sent Events (SSE) Notification Logic ---
    if (mainContent && window.EventSource) {
        const contextPath = document.body.dataset.contextPath || '';
        const eventSource = new EventSource(`${contextPath}/notifications`);

        eventSource.onopen = () => console.log("SSE connection established.");
        eventSource.onmessage = (event) => showBrowserNotification(event.data);
        eventSource.onerror = (err) => eventSource.close();
    }

    function showBrowserNotification(message) {
        if (!("Notification" in window)) return;
        
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