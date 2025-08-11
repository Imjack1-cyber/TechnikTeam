import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import apiClient from '../services/apiClient';

const hasAdminAccess = (roleName) => {
	// Frontend authorization check based on role.
	return roleName === 'ADMIN';
};

const defaultLayout = {
	sidebarPosition: 'left',
	navOrder: [], // Empty array means default order
	showHelpButton: true,
	dashboardWidgets: {
		recommendedEvents: true,
		assignedEvents: true,
		openTasks: true,
		upcomingEvents: true,
		recentConversations: true,
		upcomingMeetings: true,
		lowStockItems: false,
	},
};

const AUTH_TOKEN_KEY = 'technikteam-auth-token';

export const useAuthStore = create(
	persist(
		(set, get) => ({
			user: null,
			navigationItems: [],
			isAuthenticated: false,
			isAdmin: false,
			theme: 'light',
			layout: defaultLayout,
			lastUpdatedEvent: null, // { id: eventId, nonce: Math.random() }
			login: async (username, password) => {
				try {
					const response = await apiClient.post('/auth/login', { username, password });
					if (response.success && response.data?.session) {
						const { session, token } = response.data;
						const { user, navigation } = session;

						localStorage.setItem(AUTH_TOKEN_KEY, token);
						apiClient.setAuthToken(token);

						const newTheme = user.theme || 'light';
						document.documentElement.setAttribute('data-theme', newTheme);

						set({
							user: user,
							navigationItems: navigation,
							isAuthenticated: true,
							isAdmin: hasAdminAccess(user.roleName),
							theme: newTheme,
						});
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
					localStorage.removeItem(AUTH_TOKEN_KEY);
					apiClient.setAuthToken(null);
					set({ user: null, navigationItems: [], isAuthenticated: false, isAdmin: false, theme: 'light', layout: defaultLayout, lastUpdatedEvent: null });
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
						let userLayout = defaultLayout;
						if (user.dashboardLayout) {
							try {
								// Deep merge the saved layout with defaults to handle new widgets
								const savedLayout = JSON.parse(user.dashboardLayout);
								userLayout = {
									...defaultLayout,
									...savedLayout,
									dashboardWidgets: {
										...defaultLayout.dashboardWidgets,
										...(savedLayout.dashboardWidgets || {})
									}
								};
							} catch (e) {
								console.error("Failed to parse user layout JSON", e);
							}
						}

						set({
							user: user,
							navigationItems: result.data.navigation,
							isAuthenticated: true,
							isAdmin: hasAdminAccess(user.roleName),
							theme: newTheme,
							layout: userLayout,
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
			setLayout: async (newLayout) => {
				try {
					const result = await apiClient.put('/public/profile/layout', newLayout);
					if (result.success && result.data) {
						const updatedUser = result.data;
						let userLayout = defaultLayout;
						if (updatedUser.dashboardLayout) {
							try {
								const savedLayout = JSON.parse(updatedUser.dashboardLayout);
								userLayout = {
									...defaultLayout,
									...savedLayout,
									dashboardWidgets: {
										...defaultLayout.dashboardWidgets,
										...(savedLayout.dashboardWidgets || {})
									}
								};
							} catch (e) { console.error("Failed to parse updated layout", e); }
						}
						set({
							user: updatedUser,
							layout: userLayout,
						});
					} else {
						throw new Error(result.message || 'Server konnte das Layout nicht speichern.');
					}
				} catch (error) {
					console.error("Failed to save layout preferences:", error);
				}
			},
			triggerEventUpdate: (eventId) => {
				set({ lastUpdatedEvent: { id: eventId, nonce: Math.random() } });
			},
			setUnseenNotificationCount: (count) => {
				set(state => ({
					user: state.user ? { ...state.user, unseenNotificationsCount: count } : null
				}));
			},
			incrementUnseenNotificationCount: () => {
				set(state => ({
					user: state.user ? { ...state.user, unseenNotificationsCount: (state.user.unseenNotificationsCount || 0) + 1 } : null
				}));
			}
		}),
		{
			name: 'auth-storage',
			storage: createJSONStorage(() => localStorage),
			// We only persist non-sensitive UI state like theme. User/session data is fetched on load.
			partialize: (state) => ({ theme: state.theme }),
		}
	)
);