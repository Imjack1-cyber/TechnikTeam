import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import apiClient from '../services/apiClient';

const hasAdminAccess = (permissions) => {
	if (!permissions || permissions.length === 0) {
		return false;
	}
	if (permissions.includes('ACCESS_ADMIN_PANEL')) {
		return true;
	}
	const adminPermissions = ['_CREATE', '_UPDATE', '_DELETE', '_MANAGE', 'LOG_READ', 'REPORT_READ', 'SYSTEM_READ', 'QUALIFICATION_UPDATE'];
	return permissions.some(p => adminPermissions.some(ap => p.includes(ap)));
};

export const useAuthStore = create(
	persist(
		(set, get) => ({
			user: null,
			navigationItems: [],
			isAuthenticated: false,
			isAdmin: false,
			theme: 'light',
			login: async (username, password) => {
				try {
					// The login endpoint now returns the user object on success and sets the cookie
					const response = await apiClient.post('/auth/login', { username, password });
					if (response.success && response.data) {
						await get().fetchUserSession(); // Fetch full session data to be sure
						return true;
					}
					throw new Error(response.message || 'Login failed');
				} catch (error) {
					console.error('Login failed:', error);
					get().logout();
					throw error;
				}
			},
			logout: async () => {
				try {
					await apiClient.post('/auth/logout');
				} catch (error) {
					console.error("Logout API call failed, clearing state anyway.", error);
				} finally {
					set({ user: null, navigationItems: [], isAuthenticated: false, isAdmin: false, theme: 'light' });
					localStorage.removeItem('theme');
					document.documentElement.setAttribute('data-theme', 'light');
				}
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
				set(state => ({
					theme: newTheme,
					user: state.user ? { ...state.user, theme: newTheme } : null
				}));
				document.documentElement.setAttribute('data-theme', newTheme);
				localStorage.setItem('theme', newTheme);

				try {
					await apiClient.put('/public/profile/theme', { theme: newTheme });
				} catch (error) {
					console.error("Failed to save theme preference:", error);
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
			// We only persist non-sensitive UI state like theme. User/session data is fetched on load.
			partialize: (state) => ({ theme: state.theme }),
		}
	)
);