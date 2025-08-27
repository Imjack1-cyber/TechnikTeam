import { useState, useEffect, useCallback } from 'react';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import EventSource from 'rn-eventsource';
import AsyncStorage from '@react-native-async-storage/async-storage';

// In a real app, this would come from an environment config file
const BASE_URL = 'http://10.0.2.2:8081/TechnikTeam'; // Android emulator default

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

		let events;

		const connect = async () => {
			const token = await AsyncStorage.getItem('technikteam-auth-token');
			if (!token) {
				console.warn("[useNotifications] Cannot establish SSE connection: No auth token found.");
				return;
			}

			const sseUrl = `${BASE_URL}/api/v1/public/notifications/sse?token=${encodeURIComponent(token)}`;

			console.log(`[useNotifications] Connecting to SSE at: ${sseUrl}`);
			events = new EventSource(sseUrl);

			events.addEventListener("open", () => {
				console.log("SSE connection opened.");
			});

			events.addEventListener("message", (event) => {
				console.log("Received SSE message:", event.data);
			});

			events.addEventListener("notification", (event) => {
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

			events.addEventListener("ui_update", (event) => {
				const data = JSON.parse(event.data);
				console.log("Received UI update event:", data);
				if (data.updateType === 'EVENT_UPDATED') {
					triggerEventUpdate(data.data.eventId);
				}
			});

			events.addEventListener("error", (err) => {
				console.error("EventSource failed:", err);
				if (events) {
					events.close();
				}
			});
		};

		connect();

		return () => {
			if (events) {
				events.close();
			}
		};
	}, [isAuthenticated, addToast, triggerEventUpdate, incrementUnseenNotificationCount]);

	return { warningNotification, dismissWarning };
};