import React from 'react';

const StatusBadge = ({ status }) => {
	const getStatusClass = () => {
		switch (status) {
			case 'LAUFEND':
				return 'status-warn';
			case 'GEPLANT':
			case 'KOMPLETT':
			case 'ERLEDIGT':
				return 'status-ok';
			case 'ABGESCHLOSSEN':
			case 'ABGESAGT':
				return 'status-info';
			default:
				return 'status-info';
		}
	};

	return <span className={`status-badge ${getStatusClass()}`}>{status}</span>;
};

export default StatusBadge;