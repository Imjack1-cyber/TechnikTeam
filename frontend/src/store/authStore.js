import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import apiClient from '../services/apiClient';

// Helper function to determine if a user has admin-level access based on permissions.
const hasAdminAccess = (permissions) => {
	if (!permissions || permissions.length === 0) {
		return false;
	}
	// Check for the master admin permission
	if (permissions.includes('ACCESS_ADMIN_PANEL')) {
		return true;
	}
	// Check for any other management-level permissions
	const adminPermissions = ['_CREATE', '_UPDATE', '_DELETE', '_MANAGE', 'LOG_READ', 'REPORT_READ', 'SYSTEM_READ', 'QUALIFICATION_UPDATE'];
	return permissions.some(p => adminPermissions.some(ap => p.includes(ap)));
};

export const useAuthStore = create(
	persist(
		(set, get) => ({
			token: null,
			user: null,
			navigationItems: [],
			isAuthenticated: false,
			isAdmin: false,
			theme: 'light', // Add theme to state
			login: async (username, password) => {
				try {
					const response = await apiClient.post('/auth/login', { username, password });
					if (response.success && response.data.token) {
						const token = response.data.token;
						set({ token });
						await get().fetchUserSession();
						return true;
					}
					throw new Error(response.message || 'Login failed');
				} catch (error) {
					console.error('Login failed:', error);
					get().logout();
					throw error;
				}
			},
			logout: () => {
				set({ token: null, user: null, navigationItems: [], isAuthenticated: false, isAdmin: false, theme: 'light' });
				localStorage.removeItem('theme'); // Clean up local storage on logout
				document.documentElement.setAttribute('data-theme', 'light');
			},
			fetchUserSession: async () => {
				try {
					const result = await apiClient.get('/users/me');

					if (result.success && result.data.user && result.data.navigation) {
						const user = result.data.user;
						const newTheme = user.theme || 'light';
						set({
							user: user,
							navigationItems: result.data.navigation,
							isAuthenticated: true,
							isAdmin: hasAdminAccess(user.permissions || []),
							theme: newTheme,
						});
						// Apply theme immediately
						document.documentElement.setAttribute('data-theme', newTheme);
						localStorage.setItem('theme', newTheme);
					} else {
						throw new Error(result.message || "Invalid session data from server.");
					}

				} catch (error) {
					console.error("Could not fetch user session. Token might be invalid.", error);
					get().logout();
					throw error;
				}
			},
			setTheme: async (newTheme) => {
				const oldTheme = get().theme;
				// Optimistic UI update
				set(state => ({
					theme: newTheme,
					user: state.user ? { ...state.user, theme: newTheme } : null
				}));
				document.documentElement.setAttribute('data-theme', newTheme);
				localStorage.setItem('theme', newTheme);

				try {
					// Persist to backend
					await apiClient.put('/public/profile/theme', { theme: newTheme });
				} catch (error) {
					console.error("Failed to save theme preference:", error);
					// Revert on failure
					set(state => ({
						theme: oldTheme,
						user: state.user ? { ...state.user, theme: oldTheme } : null
					}));
					document.documentElement.setAttribute('data-theme', oldTheme);
					localStorage.setItem('theme', oldTheme);
				}
			},
		}),
		{
			name: 'auth-storage',
			storage: createJSONStorage(() => localStorage),
			partialize: (state) => ({ token: state.token }),
		}
	)
);