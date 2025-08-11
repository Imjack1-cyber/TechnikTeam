import React from 'react';
import Modal from './Modal';

const DownloadWarningModal = ({ isOpen, onClose, onConfirm, file }) => {
	if (!isOpen || !file) return null;

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Download-Warnung">
			<div style={{ textAlign: 'center' }}>
				<i className="fas fa-exclamation-triangle" style={{ fontSize: '3rem', color: 'var(--warning-color)', marginBottom: '1rem' }}></i>
				<h3>Potenziell unsichere Datei</h3>
				<p>
					Sie sind im Begriff, die Datei <strong>"{file.filename}"</strong> herunterzuladen.
				</p>
				<p>
					Dateien dieses Typs könnten potenziell schädlichen Code enthalten.
					Öffnen Sie diese Datei nur, wenn Sie der Quelle vertrauen und wissen, was Sie tun.
				</p>
				<p>
					<strong>Möchten Sie den Download fortsetzen?</strong>
				</p>
				<div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', marginTop: '1.5rem' }}>
					<button onClick={onClose} className="btn btn-secondary">Abbrechen</button>
					<button onClick={onConfirm} className="btn btn-danger">Ja, herunterladen</button>
				</div>
			</div>
		</Modal>
	);
};

export default DownloadWarningModal;