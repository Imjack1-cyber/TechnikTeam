import React, { useEffect } from 'react';

const Lightbox = ({ src, onClose }) => {
	useEffect(() => {
		const handleEscape = (event) => {
			if (event.key === 'Escape') {
				onClose();
			}
		};
		document.addEventListener('keydown', handleEscape);
		return () => {
			document.removeEventListener('keydown', handleEscape);
		};
	}, [onClose]);

	if (!src) {
		return null;
	}

	return (
		<div className="lightbox-overlay" style={{ display: 'flex' }} onClick={onClose}>
			<span className="lightbox-close" title="Schließen">×</span>
			<img
				className="lightbox-content"
				src={src}
				alt="Großansicht"
				onClick={(e) => e.stopPropagation()}
			/>
		</div>
	);
};

export default Lightbox;