import React, { useEffect, useState } from 'react';

const WarningNotification = ({ notification, onDismiss }) => {
	const [audio] = useState(new Audio('/attention.mp3'));

	useEffect(() => {
		// Play sound and flash screen on mount
		audio.play().catch(e => console.error("Audio playback failed:", e));
		document.body.classList.add('warning-flash');

		const timer = setTimeout(() => {
			document.body.classList.remove('warning-flash');
		}, 1500); // Duration of the flash animation

		// Cleanup on unmount
		return () => {
			clearTimeout(timer);
			document.body.classList.remove('warning-flash');
			audio.pause();
			audio.currentTime = 0;
		};
	}, [audio]);

	if (!notification) return null;

	return (
		<div style={{
			position: 'fixed',
			top: 0,
			left: 0,
			width: '100%',
			height: '100%',
			backgroundColor: 'rgba(0, 0, 0, 0.75)',
			display: 'flex',
			justifyContent: 'center',
			alignItems: 'center',
			zIndex: 10000,
			color: 'var(--text-color)'
		}}>
			<div className="card" style={{ maxWidth: '500px', border: '3px solid var(--danger-color)', textAlign: 'center' }}>
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