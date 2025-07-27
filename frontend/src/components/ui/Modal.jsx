import React, { useEffect } from 'react';

const Modal = ({ isOpen, onClose, title, children }) => {
	useEffect(() => {
		const handleEscape = (event) => {
			if (event.key === 'Escape') {
				onClose();
			}
		};

		if (isOpen) {
			document.addEventListener('keydown', handleEscape);
		}

		return () => {
			document.removeEventListener('keydown', handleEscape);
		};
	}, [isOpen, onClose]);

	if (!isOpen) {
		return null;
	}

	return (
		<div className="modal-overlay active" onClick={onClose}>
			<div className="modal-content" onClick={(e) => e.stopPropagation()}>
				<button
					type="button"
					className="modal-close-btn"
					aria-label="Schließen"
					onClick={onClose}
				>
					×
				</button>
				{title && <h3>{title}</h3>}
				{children}
			</div>
		</div>
	);
};

export default Modal;