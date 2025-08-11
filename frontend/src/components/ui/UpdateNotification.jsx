import React from 'react';
import { useToast } from '../../context/ToastContext';

const UpdateNotification = ({ onUpdate }) => {
	const { addToast } = useToast();

	const handleUpdate = () => {
		addToast("Anwendung wird aktualisiert...", "info");
		// The onUpdate function provided by the PWA hook will reload the page
		// to apply the new service worker.
		setTimeout(onUpdate, 1000);
	};

	return (
		<div className="toast show toast-info clickable" onClick={handleUpdate} style={{ cursor: 'pointer' }}>
			Eine neue Version ist verfügbar. Klicken, um zu aktualisieren.
			<i className="fas fa-sync-alt" style={{ marginLeft: 'auto', paddingLeft: '1rem' }}></i>
		</div>
	);
};

export default UpdateNotification;