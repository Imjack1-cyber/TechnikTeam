import React, { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import StorageItemModal from '../../components/admin/storage/StorageItemModal';

const AdminDefectivePage = () => {
	const apiCall = useCallback(() => apiClient.get('/storage?status=defective'), []);
	const { data: items, loading, error, reload } = useApi(apiCall);
	const [modalState, setModalState] = useState({ isOpen: false, item: null, mode: 'defect' });

	const openModal = (mode, item) => {
		setModalState({ isOpen: true, item, mode });
	};

	const closeModal = () => {
		setModalState({ isOpen: false, item: null, mode: 'defect' });
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	const renderTable = () => {
		if (loading) return <tr><td colSpan="4">Lade defekte Artikel...</td></tr>;
		if (error) return <tr><td colSpan="4" className="error-message">{error}</td></tr>;
		if (!items || items.length === 0) return <tr><td colSpan="4" style={{ textAlign: 'center' }}>Aktuell sind keine Artikel als defekt gemeldet.</td></tr>;

		return items.map(item => (
			<tr key={item.id}>
				<td><Link to={`/lager/details/${item.id}`}>{item.name}</Link></td>
				<td>{item.defectiveQuantity} / {item.quantity}</td>
				<td>{item.defectReason || '-'}</td>
				<td>
					<button onClick={() => openModal('defect', item)} className="btn btn-small btn-warning">Status bearbeiten</button>
				</td>
			</tr>
		));
	};

	return (
		<div>
			<h1><i className="fas fa-wrench"></i> Defekte Artikel verwalten</h1>
			<p>Hier sind alle Artikel gelistet, von denen mindestens ein Exemplar als defekt markiert wurde.</p>

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Name</th>
							<th>Defekt / Gesamt</th>
							<th>Grund</th>
							<th>Aktion</th>
						</tr>
					</thead>
					<tbody>
						{renderTable()}
					</tbody>
				</table>
			</div>

			{modalState.isOpen && (
				<StorageItemModal
					isOpen={modalState.isOpen}
					onClose={closeModal}
					onSuccess={handleSuccess}
					item={modalState.item}
					initialMode={modalState.mode}
				/>
			)}
		</div>
	);
};

export default AdminDefectivePage;