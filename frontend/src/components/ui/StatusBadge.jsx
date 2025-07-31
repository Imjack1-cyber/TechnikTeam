import React from 'react';

const StatusBadge = ({ status }) => {
	const getStatusClass = () => {
		const upperStatus = status?.toUpperCase() || '';
		switch (upperStatus) {
			case 'LAUFEND':
			case 'PENDING':
			case 'VIEWED':
			case 'PLANNED':
				return 'status-warn';
			case 'GEPLANT':
			case 'KOMPLETT':
			case 'ERLEDIGT':
			case 'APPROVED':
			case 'NEW':
				return 'status-ok';
			case 'ABGESCHLOSSEN':
			case 'ABGESAGT':
			case 'REJECTED':
			case 'COMPLETED':
				return 'status-info';
			default:
				return 'status-info';
		}
	};

	return <span className={`status-badge ${getStatusClass()}`}>{status}</span>;
};

export default StatusBadge;