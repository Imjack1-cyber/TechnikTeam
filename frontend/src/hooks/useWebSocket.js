import { useState, useEffect, useRef, useCallback } from 'react';
import { Platform } from 'react-native';
import { getToken } from '../lib/storage';
import { useAuthStore } from '../store/authStore';

const useWebSocket = (url, onMessage, dependencies = []) => {
	const [readyState, setReadyState] = useState(WebSocket.CONNECTING);
	const socketRef = useRef(null);
	const onMessageCallback = useCallback(onMessage, []);

	useEffect(() => {
		let reconnectTimeout;
		const connect = async () => {
            const backendMode = useAuthStore.getState().backendMode;
            const host = backendMode === 'dev' ? 'technikteamdev.duckdns.org' : 'technikteam.duckdns.org';

			const token = await getToken();
			if (!url || !token) {
				if (socketRef.current) socketRef.current.close(1000, "URL or token changed to null");
				setReadyState(WebSocket.CLOSED);
				return;
			}
			const authenticatedUrl = `${url}?token=${encodeURIComponent(token)}`;
			let finalUrl;

			if (Platform.OS === 'web') {
				const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
				const webHost = window.location.host;
                // For web, connect to the same host via the proxy path
				finalUrl = `${protocol}//${webHost}/TechnikTeam${authenticatedUrl}`;
			} else {
                // For native, connect directly to the backend host
				finalUrl = `wss://${host}/TechnikTeam${authenticatedUrl}`;
			}

			console.log(`Attempting to connect to WebSocket at: ${finalUrl}`);
			const socket = new WebSocket(finalUrl);
			socketRef.current = socket;

			socket.onopen = () => {
				console.log('WebSocket-Verbindung hergestellt.');
				setReadyState(WebSocket.OPEN);
			};
			socket.onmessage = (event) => {
				try {
					const data = JSON.parse(event.data);
					if (onMessageCallback) onMessageCallback(data);
				} catch (error) {
					console.error('Fehler beim Parsen der WebSocket-Nachricht:', error);
				}
			};
			socket.onclose = (event) => {
				if (event.code === 1000 && event.reason === "URL or token changed to null") {
					console.log("WebSocket connection intentionally closed.");
				} else if (event.code === 4001 || event.code === 403) {
					console.error('WebSocket-Verbindung aufgrund von Authentifizierungs-/Autorisierungsfehler geschlossen.');
				} else {
					console.warn('WebSocket-Verbindung geschlossen. Versuche erneute Verbindung...');
					reconnectTimeout = setTimeout(connect, 5000);
				}
				setReadyState(WebSocket.CLOSED);
			};
			socket.onerror = (error) => {
				console.error('WebSocket-Fehler:', error.message);
				setReadyState(WebSocket.CLOSED);
				socket.close();
			};
		};
		connect();
		return () => {
			clearTimeout(reconnectTimeout);
			if (socketRef.current) {
				socketRef.current.onclose = null;
				socketRef.current.close();
			}
		};
	// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [url, onMessageCallback, ...dependencies]);

	const sendMessage = (messageObject) => {
		if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) {
			socketRef.current.send(JSON.stringify(messageObject));
		} else {
			console.error('WebSocket ist nicht geöffnet. Nachricht kann nicht gesendet werden.');
		}
	};

	return { readyState, sendMessage };
};

export default useWebSocket;