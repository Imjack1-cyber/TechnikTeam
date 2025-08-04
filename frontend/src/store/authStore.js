import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import apiClient from '../services/apiClient';

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
					throw new Error(response.message || 'Anmeldung fehlgeschlagen');
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
					localStorage.removeItem('auth-storage');
					document.documentElement.setAttribute('data-theme', 'light');
				}
			},
			fetchUserSession: async () => {
				try {
					const result = await apiClient.get('/auth/me');

					if (result.success && result.data.user && result.data.navigation) {
						const user = result.data.user;
						const newTheme = user.theme || 'light';
						set({
							user: user,
							navigationItems: result.data.navigation,
							isAuthenticated: true,
							isAdmin: user.roleName === 'ADMIN',
							theme: newTheme,
						});
						document.documentElement.setAttribute('data-theme', newTheme);
					} else {
						throw new Error(result.message || "Ungültige Sitzungsdaten vom Server.");
					}

				} catch (error) {
					console.error("Could not fetch user session. Token might be invalid.", error);
					get().logout();
					throw error;
				}
			},
			setTheme: async (newTheme) => {
				try {
					const result = await apiClient.put('/public/profile/theme', { theme: newTheme });
					if (result.success && result.data) {
						const updatedUser = result.data;
						set({
							user: updatedUser,
							theme: updatedUser.theme,
						});
						document.documentElement.setAttribute('data-theme', updatedUser.theme);
					} else {
						throw new Error(result.message || 'Server konnte das Theme nicht speichern.');
					}
				} catch (error) {
					console.error("Failed to save theme preference:", error);
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