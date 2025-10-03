import { useState, useEffect, useCallback } from 'react';
import { Platform } from 'react-native';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import { useUIStore } from '../store/uiStore';
import RNEventSource from 'react-native-sse';
import { getToken } from '../lib/storage';

const getSseBaseUrl = () => {
    const mode = useAuthStore.getState().backendMode;
    if (Platform.OS === 'web') {
        // On the web, the browser handles the relative path correctly.
        // We just need the context path.
        return '/TechnikTeam';
    }
    // For native, we need the full absolute URL.
    const host = mode === 'dev' ? 'technikteamdev.qs0.de' : 'technikteam.qs0.de';
    return `https://${host}/TechnikTeam`;
};

export const useNotifications = () => {
	const { addToast } = useToast();
	const [warningNotification, setWarningNotification] = useState(null);
	const { isAuthenticated, incrementUnseenNotificationCount, setMaintenanceStatus } = useAuthStore(state => ({
		isAuthenticated: state.isAuthenticated,
		incrementUnseenNotificationCount: state.incrementUnseenNotificationCount,
        setMaintenanceStatus: state.setMaintenanceStatus,
	}));
    const triggerRefetch = useUIStore(state => state.triggerRefetch);

	const dismissWarning = useCallback(() => {
		setWarningNotification(null);
	}, []);

	useEffect(() => {
		if (!isAuthenticated) {
			return;
		}

		let es;

		const connect = async () => {
			const token = await getToken();
			if (!token) {
				console.warn("[useNotifications] Cannot establish SSE connection: No auth token found.");
				return;
			}

			const baseUrl = getSseBaseUrl();
			const sseUrl = `${baseUrl}/api/v1/public/notifications/sse?token=${encodeURIComponent(token)}`;

			console.log(`[useNotifications] Connecting to SSE at: ${sseUrl}`);
			// react-native-sse polyfills EventSource for native, and uses the browser's native one on web.
			es = new RNEventSource(sseUrl);

			es.addEventListener("open", () => {
				console.log("SSE connection opened.");
			});

			es.addEventListener("message", (event) => {
				console.log("Received SSE message:", event.data);
			});

			es.addEventListener("notification", (event) => {
				const data = JSON.parse(event.data);
				incrementUnseenNotificationCount();
				if (data.level === 'Warning') {
					setWarningNotification(data);
				} else {
					addToast(
						`${data.title}: ${data.description}`,
						data.level === 'Important' ? 'error' : 'info',
						data.url || null
					);
				}
			});

			es.addEventListener("ui_update", (event) => {
				const data = JSON.parse(event.data);
				console.log("Received UI update event:", data);
				if (data.entity) {
                    triggerRefetch(data.entity);
                }
			});

            es.addEventListener("system_status_update", (event) => {
                const data = JSON.parse(event.data);
                console.log("Received system status update:", data);
                setMaintenanceStatus(data);
            });

			es.addEventListener("error", (err) => {
				console.error("EventSource failed:", err);
				if (es) {
					es.close();
				}
			});
		};

		connect();

		return () => {
			if (es) {
				es.close();
			}
		};
	}, [isAuthenticated, addToast, triggerRefetch, incrementUnseenNotificationCount, setMaintenanceStatus]);

	return { warningNotification, dismissWarning };
};