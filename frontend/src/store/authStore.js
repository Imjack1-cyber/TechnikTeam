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
					const result = await apiClient.get('/users/me');

					if (result.success && result.data.user && result.data.navigation) {
						set({
							user: result.data.user,
							navigationItems: result.data.navigation,
							isAuthenticated: true,
							isAdmin: hasAdminAccess(result.data.user.permissions || [])
						});
					} else {
						throw new Error(result.message || "Invalid session data from server.");
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
			partialize: (state) => ({ token: state.token }),
		}
	)
);