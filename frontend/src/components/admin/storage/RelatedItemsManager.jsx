import React, { useState, useEffect, useCallback } from 'react';
import useApi from '../../../hooks/useApi';
import apiClient from '../../../services/apiClient';

const RelatedItemsManager = ({ item, allItems, onSave, onCancel }) => {
	const relationsApiCall = useCallback(() => apiClient.get(`/admin/storage/${item.id}/relations`), [item.id]);
	const { data: relatedItems, loading, error } = useApi(relationsApiCall);
	const [selectedIds, setSelectedIds] = useState(new Set());
	const [isSubmitting, setIsSubmitting] = useState(false);

	useEffect(() => {
		if (relatedItems) {
			setSelectedIds(new Set(relatedItems.map(i => i.id)));
		}
	}, [relatedItems]);

	const handleToggle = (itemId) => {
		setSelectedIds(prev => {
			const newSet = new Set(prev);
			if (newSet.has(itemId)) {
				newSet.delete(itemId);
			} else {
				newSet.add(itemId);
			}
			return newSet;
		});
	};

	const handleSave = async () => {
		setIsSubmitting(true);
		try {
			await apiClient.put(`/admin/storage/${item.id}/relations`, { relatedItemIds: Array.from(selectedIds) });
			onSave();
		} catch (err) {
			console.error("Failed to save related items", err);
		} finally {
			setIsSubmitting(false);
		}
	};

	const availableItems = allItems.filter(i => i.id !== item.id);

	return (
		<div>
			{loading && <p>Lade Beziehungen...</p>}
			{error && <p className="error-message">{error}</p>}
			<div className="form-group" style={{ maxHeight: '40vh', overflowY: 'auto', border: '1px solid var(--border-color)', borderRadius: 'var(--border-radius)', padding: '0.5rem' }}>
				{availableItems.map(i => (
					<label key={i.id} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
						<input type="checkbox" checked={selectedIds.has(i.id)} onChange={() => handleToggle(i.id)} />
						{i.name}
					</label>
				))}
			</div>
			<div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
				<button type="button" className="btn btn-secondary" onClick={onCancel}>Abbrechen</button>
				<button type="button" className="btn btn-success" onClick={handleSave} disabled={isSubmitting}>
					{isSubmitting ? 'Speichern...' : 'Beziehungen speichern'}
				</button>
			</div>
		</div>
	);
};

export default RelatedItemsManager;