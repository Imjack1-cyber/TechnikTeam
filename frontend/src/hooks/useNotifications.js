import { useState, useEffect, useCallback } from 'react';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';

export const useNotifications = () => {
	const { addToast } = useToast();
	const [warningNotification, setWarningNotification] = useState(null);
	const { isAuthenticated, triggerEventUpdate } = useAuthStore(state => ({
		isAuthenticated: state.isAuthenticated,
		triggerEventUpdate: state.triggerEventUpdate,
	}));

	const dismissWarning = useCallback(() => {
		setWarningNotification(null);
	}, []);

	useEffect(() => {
		if (!isAuthenticated) {
			return;
		}

		const events = new EventSource('/api/v1/admin/notifications/sse');

		events.onmessage = (event) => {
			console.log("Received SSE message:", event.data);
		};

		events.addEventListener("notification", (event) => {
			const data = JSON.parse(event.data);
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
	}, [isAuthenticated, addToast, triggerEventUpdate]);

	return { warningNotification, dismissWarning };
};