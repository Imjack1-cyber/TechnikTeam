import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import apiClient from '../services/apiClient';
import { jwtDecode } from 'jwt-decode';

const hasAdminAccess = (permissions) => {
	if (!permissions || permissions.length === 0) {
		return false;
	}
	if (permissions.includes('ACCESS_ADMIN_PANEL')) {
		return true;
	}
	// A simplified check that mirrors the backend's User.hasAdminAccess() logic
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
				set({ token: null, user: null, navigationItems: [], isAuthenticated: false, isAdmin: false });
			},
			fetchUserSession: async () => {
				try {
					const { data } = await apiClient.get('/users/me');

					if (data.user && data.navigation) {
						set({
							user: data.user,
							navigationItems: data.navigation,
							isAuthenticated: true,
							isAdmin: hasAdminAccess(data.user.permissions || [])
						});
					} else {
						throw new Error("Invalid session data from server.");
					}

				} catch (error) {
					console.error("Could not fetch user session. Token might be invalid.", error);
					get().logout();
					throw error;
				}
			},
		}),
		{
			name: 'auth-storage',
			storage: createJSONStorage(() => localStorage),
			partialize: (state) => ({ token: state.token }), // Only persist the token
		}
	)
);