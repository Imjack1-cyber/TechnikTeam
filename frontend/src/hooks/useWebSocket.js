import { useState, useEffect, useRef } from 'react';
import { useAuthStore } from '../store/authStore';

/**
 * A custom hook to manage a WebSocket connection.
 * @param {string} url - The WebSocket URL to connect to.
 * @param {Function} onMessage - Callback function to handle incoming messages.
 * @returns {object} An object containing the WebSocket ready state and a sendMessage function.
 */
const useWebSocket = (url, onMessage) => {
	const [readyState, setReadyState] = useState(WebSocket.CONNECTING);
	const socketRef = useRef(null);
	const token = useAuthStore.getState().token;

	useEffect(() => {
		if (!url || !token) return;

		const connect = () => {
			// Append token for authentication during handshake
			const authenticatedUrl = new URL(url);
			authenticatedUrl.searchParams.append('token', token);
			const socket = new WebSocket(authenticatedUrl);
			socketRef.current = socket;

			socket.onopen = () => {
				console.log('WebSocket connection established.');
				setReadyState(WebSocket.OPEN);
			};

			socket.onmessage = (event) => {
				try {
					const data = JSON.parse(event.data);
					if (onMessage) {
						onMessage(data);
					}
				} catch (error) {
					console.error('Error parsing WebSocket message:', error);
				}
			};

			socket.onclose = (event) => {
				if (event.code === 4001) { // Custom code for auth failure
					console.error('WebSocket connection closed due to authentication failure.');
					// Don't reconnect on auth failure
				} else {
					console.warn('WebSocket connection closed. Attempting to reconnect...');
					setTimeout(connect, 5000);
				}
				setReadyState(WebSocket.CLOSED);
			};

			socket.onerror = (error) => {
				console.error('WebSocket error:', error);
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
	}, [url, onMessage, token]);

	const sendMessage = (messageObject) => {
		if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) {
			socketRef.current.send(JSON.stringify(messageObject));
		} else {
			console.error('WebSocket is not open. Cannot send message.');
		}
	};

	return { readyState, sendMessage };
};

export default useWebSocket;