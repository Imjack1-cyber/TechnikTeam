import React from 'react';

const MessageStatus = ({ status, isSentByMe }) => {
	if (!isSentByMe) {
		return null;
	}

	const statusIcon = () => {
		switch (status) {
			case 'SENT':
				return <i className="fas fa-check" title="Gesendet"></i>;
			case 'DELIVERED':
				// For simplicity, we'll treat SENT as DELIVERED visually until READ.
				// A true DELIVERED state would require more complex server-side presence tracking.
				return <i className="fas fa-check-double" title="Zugestellt"></i>;
			case 'READ':
				return <i className="fas fa-check-double" style={{ color: 'var(--info-color)' }} title="Gelesen"></i>;
			default:
				return <i className="fas fa-clock" title="Senden..."></i>;
		}
	};

	return (
		<span className="message-status">
			{statusIcon()}
		</span>
	);
};

export default MessageStatus;