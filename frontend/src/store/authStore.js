import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import apiClient from '../services/apiClient';
import { storage, setToken, removeToken } from '../lib/storage';
import { Platform } from 'react-native';

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

export const useAuthStore = create(
	persist(
		(set, get) => ({
			user: null,
			navigationItems: [],
			isAuthenticated: false,
			isAdmin: false,
			theme: 'light',
            backendMode: 'prod', // 'prod' or 'dev'
			layout: defaultLayout,
			maintenanceStatus: { mode: 'OFF', message: '' },
			previousLogin: null,
            completeLogin: async (loginData) => {
                const { session, token } = loginData;
                const { user, navigation, maintenanceStatus, previousLogin } = session;

                await setToken(token);
                apiClient.setAuthToken(token);

                const newTheme = user.theme || 'light';
				let userLayout = defaultLayout;
				if (user.dashboardLayout) {
					try {
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
						console.error("Failed to parse user layout JSON on login", e);
					}
				}

                set({
                    user: user,
                    navigationItems: navigation,
                    isAuthenticated: true,
                    isAdmin: hasAdminAccess(user.roleName),
                    theme: newTheme,
					layout: userLayout,
                    maintenanceStatus: maintenanceStatus || { mode: 'OFF', message: '' },
                    previousLogin: previousLogin,
                });
            },
			login: async (username, password) => {
				try {
                    const clientType = Platform.OS === 'web' ? 'web' : 'native';
					const response = await apiClient.post('/auth/login', { username, password, clientType });
					if (response.success && response.data) {
						if (response.message === '2FA_REQUIRED') {
							return { status: '2FA_REQUIRED', token: response.data.token };
						}
						
                        await get().completeLogin(response.data);
						return { status: 'SUCCESS' };
					}
					throw new Error(response.message || 'Anmeldung fehlgeschlagen');
				} catch (error) {
					console.error('Login failed:', error);
					get().logout();
					throw error;
				}
			},
            completePasskeyLogin: (loginData) => {
                return get().completeLogin(loginData);
            },
			logout: async () => {
				try {
					await apiClient.post('/auth/logout');
				} catch (error) {
					console.error("Logout API call failed, clearing state anyway.", error);
				} finally {
					await removeToken();
					apiClient.setAuthToken(null);
                    // Preserve theme and backendMode on logout
					set(state => ({ user: null, navigationItems: [], isAuthenticated: false, isAdmin: false, layout: defaultLayout, maintenanceStatus: { mode: 'OFF', message: '' }, previousLogin: null, theme: state.theme, backendMode: state.backendMode }));
				}
			},
			fetchUserSession: async () => {
				try {
					const result = await apiClient.get('/auth/me');

					if (result.success && result.data.user && result.data.navigation) {
						const { user, navigation, maintenanceStatus } = result.data;
						const newTheme = user.theme || 'light';
						let userLayout = defaultLayout;
						if (user.dashboardLayout) {
							try {
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
							navigationItems: navigation,
							isAuthenticated: true,
							isAdmin: hasAdminAccess(user.roleName),
							theme: newTheme,
							layout: userLayout,
							maintenanceStatus: maintenanceStatus || { mode: 'OFF', message: '' },
						});
					} else {
						throw new Error(result.message || "Ungültige Sitzungsdaten vom Server.");
					}

				} catch (error) {
					console.error("Could not fetch user session. Token might be invalid.", error);
					get().logout();
					throw error;
				}
			},
            setBackendMode: (mode) => {
                const currentMode = get().backendMode;
                if (mode !== currentMode) {
                    console.log(`Switching backend from ${currentMode} to ${mode}`);
                    get().logout(); // Logout to clear session from old environment
                    set({ backendMode: mode });
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
			setUnseenNotificationCount: (count) => {
				set(state => ({
					user: state.user ? { ...state.user, unseenNotificationsCount: count } : null
				}));
			},
			incrementUnseenNotificationCount: () => {
				set(state => ({
					user: state.user ? { ...state.user, unseenNotificationsCount: (state.user.unseenNotificationsCount || 0) + 1 } : null
				}));
			},
			setMaintenanceStatus: (status) => {
				set(state => ({ maintenanceStatus: { ...state.maintenanceStatus, ...status } }));
			},
		}),
		{
			name: 'auth-storage',
			storage: createJSONStorage(() => storage), // Use our universal storage wrapper
			partialize: (state) => ({ theme: state.theme, backendMode: state.backendMode }),
		}
	)
);