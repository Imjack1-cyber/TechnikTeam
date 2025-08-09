import React, { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import StorageItemModal from '../../components/admin/storage/StorageItemModal';
import { useToast } from '../../context/ToastContext';

const AdminDefectivePage = () => {
	const apiCall = useCallback(() => apiClient.get('/storage'), []);
	const { data: allItems, loading, error, reload } = useApi(apiCall);
	const [modalState, setModalState] = useState({ isOpen: false, item: null, mode: 'defect' });
	const { addToast } = useToast();

	const openModal = (mode, item) => {
		setModalState({ isOpen: true, item, mode });
	};

	const closeModal = () => {
		setModalState({ isOpen: false, item: null, mode: 'defect' });
	};

	const handleSuccess = () => {
		addToast('Status erfolgreich aktualisiert', 'success');
		closeModal();
		reload();
	};

	const items = allItems?.filter(item => item.defectiveQuantity > 0);

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
					<button onClick={() => openModal('repair', item)} className="btn btn-small btn-success">Repariert</button>
					<button onClick={() => openModal('defect', item)} className="btn btn-small btn-warning" style={{ marginLeft: '0.5rem' }}>Status bearbeiten</button>
				</td>
			</tr>
		));
	};

	const renderMobileList = () => {
		if (loading) return <p>Lade defekte Artikel...</p>;
		if (error) return <p className="error-message">{error}</p>;
		if (!items || items.length === 0) return <div className="card"><p>Aktuell sind keine Artikel als defekt gemeldet.</p></div>;

		return items.map(item => (
			<div className="list-item-card" key={item.id}>
				<h3 className="card-title"><Link to={`/lager/details/${item.id}`}>{item.name}</Link></h3>
				<div className="card-row"><strong>Defekt / Gesamt:</strong> <span>{item.defectiveQuantity} / {item.quantity}</span></div>
				<div className="card-row"><strong>Grund:</strong> <span>{item.defectReason || '-'}</span></div>
				<div className="card-actions">
					<button onClick={() => openModal('repair', item)} className="btn btn-small btn-success">Repariert</button>
					<button onClick={() => openModal('defect', item)} className="btn btn-small btn-warning">Status bearbeiten</button>
				</div>
			</div>
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

			<div className="mobile-card-list">
				{renderMobileList()}
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