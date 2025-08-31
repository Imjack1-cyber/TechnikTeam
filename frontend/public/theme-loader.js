// This script runs immediately to prevent a "flash" of the wrong theme.
(function() {
    const savedTheme = localStorage.getItem('auth-storage') 
        ? JSON.parse(localStorage.getItem('auth-storage')).state.theme 
        : 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
})();