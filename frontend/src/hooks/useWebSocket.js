import { useState, useEffect, useRef, useCallback } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

// In a real app, this would come from an environment config file
const WS_BASE_URL = 'ws://10.0.2.2:8081/TechnikTeam'; // Android emulator default

/**
 * A custom hook to manage a WebSocket connection in React Native.
 * @param {string} url - The WebSocket URL path (e.g., /ws/chat/1).
 * @param {Function} onMessage - Callback function to handle incoming messages.
 * @param {Array} dependencies - An array of dependencies that, when changed, will trigger a reconnect.
 * @returns {object} An object containing the WebSocket ready state and a sendMessage function.
 */
const useWebSocket = (url, onMessage, dependencies = []) => {
	const [readyState, setReadyState] = useState(WebSocket.CONNECTING);
	const socketRef = useRef(null);

	const onMessageCallback = useCallback(onMessage, []);

	useEffect(() => {
		let reconnectTimeout;

		const connect = async () => {
			const token = await AsyncStorage.getItem('technikteam-auth-token');
			if (!url || !token) {
				if (socketRef.current) {
					socketRef.current.close(1000, "URL or token changed to null");
					socketRef.current = null;
				}
				setReadyState(WebSocket.CLOSED);
				return;
			}

			const authenticatedUrl = `${url}?token=${encodeURIComponent(token)}`;
			const finalUrl = `${WS_BASE_URL}${authenticatedUrl}`;
			
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
					if (onMessageCallback) {
						onMessageCallback(data);
					}
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
				socket.close(); // Ensure it's closed before reconnecting
			};
		};

		connect();

		return () => {
			clearTimeout(reconnectTimeout);
			if (socketRef.current) {
				socketRef.current.onclose = null; // Prevent reconnect attempts on component unmount
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