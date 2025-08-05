import React, { useState, useCallback, useEffect } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import useWebSocket from '../../hooks/useWebSocket'; // Assuming you have a generic WS hook

const ChecklistTab = ({ event, user }) => {
	const { addToast } = useToast();
	const [checklistItems, setChecklistItems] = useState([]);

	const checklistApiCall = useCallback(() => apiClient.get(`/events/${event.id}/checklist`), [event.id]);
	const { data: initialItems, loading, error, reload } = useApi(checklistApiCall);

	useEffect(() => {
		if (initialItems) {
			setChecklistItems(initialItems);
		}
	}, [initialItems]);

	const handleStatusUpdate = (updatedItem) => {
		setChecklistItems(currentItems =>
			currentItems.map(item => item.id === updatedItem.id ? updatedItem : item)
		);
	};

	// Setup WebSocket for real-time updates
	const websocketUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws/checklist/${event.id}`;
	useWebSocket(websocketUrl, (message) => {
		if (message.type === 'checklist_update') {
			handleStatusUpdate(message.payload);
		}
	});


	const handleStatusChange = async (itemId, newStatus) => {
		try {
			const result = await apiClient.put(`/events/${event.id}/checklist/${itemId}/status`, { status: newStatus });
			if (!result.success) { // WebSocket will handle the success update
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(`Fehler beim Aktualisieren: ${err.message}`, 'error');
		}
	};

	const handleGenerateChecklist = async () => {
		try {
			const result = await apiClient.post(`/events/${event.id}/checklist/generate`);
			if (result.success) {
				addToast('Checkliste erfolgreich aus Reservierungen generiert.', 'success');
				reload();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(`Fehler: ${err.message}`, 'error');
		}
	};

	if (loading) return <p>Lade Checkliste...</p>;
	if (error) return <p className="error-message">{error}</p>;

	return (
		<div>
			<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
				<p>Haken Sie Artikel beim Ein- und Ausladen ab, um den Überblick zu behalten.</p>
				<button onClick={handleGenerateChecklist} className="btn btn-secondary">
					<i className="fas fa-sync"></i> Liste aus Reservierungen generieren
				</button>
			</div>

			<div className="table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Artikel</th>
							<th>Menge</th>
							<th>Status</th>
							<th>Zuletzt geändert</th>
						</tr>
					</thead>
					<tbody>
						{checklistItems.length === 0 ? (
							<tr><td colSpan="4">Keine Artikel auf der Checkliste. Generieren Sie eine aus den Materialreservierungen.</td></tr>
						) : (
							checklistItems.map(item => (
								<tr key={item.id}>
									<td>{item.itemName}</td>
									<td>{item.quantity}</td>
									<td>
										<select
											value={item.status}
											onChange={(e) => handleStatusChange(item.id, e.target.value)}
											className="form-group"
											style={{ marginBottom: 0 }}
										>
											<option value="PENDING">Ausstehend</option>
											<option value="PACKED_OUT">Eingepackt (Load-Out)</option>
											<option value="RETURNED_CHECKED">Zurück & OK</option>
											<option value="RETURNED_DEFECT">Zurück & Defekt</option>
										</select>
									</td>
									<td>{item.lastUpdatedByUsername || 'N/A'} am {new Date(item.lastUpdatedAt).toLocaleTimeString('de-DE')}</td>
								</tr>
							))
						)}
					</tbody>
				</table>
			</div>
		</div>
	);
};

export default ChecklistTab;