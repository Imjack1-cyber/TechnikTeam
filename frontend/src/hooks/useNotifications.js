import { useState, useEffect, useCallback } from 'react';
import { Platform } from 'react-native';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import RNEventSource from 'react-native-sse';
import { getToken } from '../lib/storage';

const ANDROID_SSE_URL = 'http://10.0.2.2:8081/TechnikTeam';
const WEB_SSE_URL = ''; // For web, it's relative to the current host

const BASE_URL = Platform.OS === 'web' ? WEB_SSE_URL : ANDROID_SSE_URL;

export const useNotifications = () => {
	const { addToast } = useToast();
	const [warningNotification, setWarningNotification] = useState(null);
	const { isAuthenticated, triggerEventUpdate, incrementUnseenNotificationCount } = useAuthStore(state => ({
		isAuthenticated: state.isAuthenticated,
		triggerEventUpdate: state.triggerEventUpdate,
		incrementUnseenNotificationCount: state.incrementUnseenNotificationCount,
	}));

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

			// For web, the browser constructs the full URL including protocol and host.
			const sseUrl = `${BASE_URL}/TechnikTeam/api/v1/public/notifications/sse?token=${encodeURIComponent(token)}`;

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
				if (data.updateType === 'EVENT_UPDATED') {
					triggerEventUpdate(data.data.eventId);
				}
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
	}, [isAuthenticated, addToast, triggerEventUpdate, incrementUnseenNotificationCount]);

	return { warningNotification, dismissWarning };
};