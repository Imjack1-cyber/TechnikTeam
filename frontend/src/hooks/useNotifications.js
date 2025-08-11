import { useState, useEffect, useCallback } from 'react';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';

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

		const token = localStorage.getItem('technikteam-auth-token');
		if (!token) {
			console.warn("[useNotifications] Cannot establish SSE connection: No auth token found.");
			return;
		}

		// Construct the correct URL for EventSource with the token as a query parameter
		const ssePath = `/api/v1/public/notifications/sse?token=${encodeURIComponent(token)}`;
		let sseUrl;
		if (import.meta.env.PROD) {
			// In production, the path is relative to the origin
			sseUrl = `/TechnikTeam${ssePath}`;
		} else {
			// In development, it's an absolute path to be caught by the Vite proxy
			sseUrl = ssePath;
		}

		console.log(`[useNotifications] Connecting to SSE at: ${sseUrl}`);
		const events = new EventSource(sseUrl);

		events.onmessage = (event) => {
			console.log("Received SSE message:", event.data);
		};

		events.addEventListener("notification", (event) => {
			const data = JSON.parse(event.data);
			incrementUnseenNotificationCount();
			if (data.level === 'Warning') {
				setWarningNotification(data);
			} else {
				addToast(
					`${data.title}: ${data.description}`,
					data.level === 'Important' ? 'error' : 'info',
					data.url || null // Pass the URL to the toast
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

		events.onerror = (err) => {
			console.error("EventSource failed:", err);
			events.close();
		};

		return () => {
			events.close();
		};
	}, [isAuthenticated, addToast, triggerEventUpdate, incrementUnseenNotificationCount]);

	return { warningNotification, dismissWarning };
};