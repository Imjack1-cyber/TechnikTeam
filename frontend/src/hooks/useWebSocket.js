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
			const socket = new WebSocket(url);
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

			socket.onclose = () => {
				console.warn('WebSocket connection closed. Attempting to reconnect...');
				setReadyState(WebSocket.CLOSED);
				setTimeout(connect, 5000);
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
				socketRef.current.onclose = null;
				socketRef.current.close();
			}
		};
	}, [url, onMessage]);

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