import React, { useEffect, useState } from 'react';

const WarningNotification = ({ notification, onDismiss }) => {
	const [audio] = useState(new Audio('/attention.mp3'));

	useEffect(() => {
		// Configure audio to loop
		audio.loop = true;
		// Play sound and apply flash class
		audio.play().catch(e => console.error("Audio playback failed:", e));
		document.body.classList.add('warning-flash');

		// Cleanup function: stop audio and remove class when component unmounts (or is dismissed)
		return () => {
			audio.pause();
			audio.currentTime = 0;
			document.body.classList.remove('warning-flash');
		};
	}, [audio]);

	if (!notification) return null;

	return (
		<div className="modal-overlay active" style={{ zIndex: 10000 }}>
			<div className="modal-content" style={{ maxWidth: '500px', border: '3px solid var(--danger-color)', textAlign: 'center' }}>
				<i className="fas fa-exclamation-triangle" style={{ fontSize: '4rem', color: 'var(--danger-color)', marginBottom: '1rem' }}></i>
				<h1 style={{ color: 'var(--danger-color)' }}>{notification.title}</h1>
				<p style={{ fontSize: '1.2rem' }}>{notification.description}</p>
				<button onClick={onDismiss} className="btn btn-danger" style={{ marginTop: '1.5rem' }}>
					Verstanden
				</button>
			</div>
		</div>
	);
};

export default WarningNotification;