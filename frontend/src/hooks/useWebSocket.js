import { useState, useEffect, useRef } from 'react';

/**
 * A custom hook to manage a WebSocket connection.
 * @param {string} url - The WebSocket URL to connect to.
 * @param {Function} onMessage - Callback function to handle incoming messages.
 * @returns {object} An object containing the WebSocket ready state and a sendMessage function.
 */
const useWebSocket = (url, onMessage) => {
	const [readyState, setReadyState] = useState(WebSocket.CONNECTING);
	const socketRef = useRef(null);

	useEffect(() => {
		if (!url) return;

		const connect = () => {
			// Authentication is now handled by the HttpOnly cookie, so no token is needed in the URL.
			const socket = new WebSocket(url);
			socketRef.current = socket;

			socket.onopen = () => {
				console.log('WebSocket-Verbindung hergestellt.');
				setReadyState(WebSocket.OPEN);
			};

			socket.onmessage = (event) => {
				try {
					const data = JSON.parse(event.data);
					if (onMessage) {
						onMessage(data);
					}
				} catch (error) {
					console.error('Fehler beim Parsen der WebSocket-Nachricht:', error);
				}
			};

			socket.onclose = (event) => {
				if (event.code === 4001 || event.code === 403) {
					console.error('WebSocket-Verbindung aufgrund von Authentifizierungs-/Autorisierungsfehler geschlossen.');
				} else {
					console.warn('WebSocket-Verbindung geschlossen. Versuche erneute Verbindung...');
					setTimeout(connect, 5000);
				}
				setReadyState(WebSocket.CLOSED);
			};

			socket.onerror = (error) => {
				console.error('WebSocket-Fehler:', error);
				setReadyState(WebSocket.CLOSED);
				socket.close();
			};
		};

		connect();

		return () => {
			if (socketRef.current) {
				socketRef.current.onclose = null; // Prevent reconnect attempts on component unmount
				socketRef.current.close();
			}
		};
	}, [url, onMessage]);

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