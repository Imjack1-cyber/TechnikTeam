import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import useApi from '@/hooks/useApi';
import apiClient from '@/services/apiClient';
import StorageItemModal from '@/components/admin/storage/StorageItemModal';
import Lightbox from '@/components/ui/Lightbox';
import StatusBadge from '@/components/ui/StatusBadge';

const AdminStoragePage = () => {
	const { data: items, loading, error, reload } = useApi(() => apiClient.get('/storage'));
	const [modalState, setModalState] = useState({ isOpen: false, item: null, mode: 'edit' });
	const [lightboxSrc, setLightboxSrc] = useState('');

	const openModal = (mode, item = null) => {
		setModalState({ isOpen: true, item, mode });
	};

	const closeModal = () => {
		setModalState({ isOpen: false, item: null, mode: 'edit' });
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	const handleDelete = async (item) => {
		if (window.confirm(`Artikel '${item.name}' wirklich löschen?`)) {
			try {
				await apiClient.delete(`/storage/${item.id}`);
				reload();
			} catch (err) {
				alert(`Fehler: ${err.message}`);
			}
		}
	};

	return (
		<div>
			<h1><i className="fas fa-warehouse"></i> Lagerverwaltung</h1>
			<div className="table-controls">
				<button onClick={() => openModal('create')} className="btn btn-success">
					<i className="fas fa-plus"></i> Neuen Artikel anlegen
				</button>
			</div>

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Name</th>
							<th>Ort</th>
							<th>Verfügbar</th>
							<th>Status</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{loading && <tr><td colSpan="5">Lade Artikel...</td></tr>}
						{error && <tr><td colSpan="5" className="error-message">{error}</td></tr>}
						{items?.map(item => (
							<tr key={item.id}>
								<td className="item-name-cell">
									<Link to={`/lager/details/${item.id}`}>{item.name}</Link>
									{item.imagePath && (
										<button className="camera-btn" onClick={() => setLightboxSrc(`/api/v1/public/files/images/${item.imagePath}`)}>
											<i className="fas fa-camera"></i>
										</button>
									)}
								</td>
								<td>{item.location}</td>
								<td>
									{item.availableQuantity}/{item.maxQuantity}
									{item.defectiveQuantity > 0 && <span className="text-danger"> ({item.defectiveQuantity} def.)</span>}
								</td>
								<td><StatusBadge status={item.status} /></td>
								<td style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
									<button onClick={() => openModal('edit', item)} className="btn btn-small">Bearbeiten</button>
									<button onClick={() => openModal('defect', item)} className="btn btn-small btn-warning">Defekt</button>
									{item.defectiveQuantity > 0 && (
										<button onClick={() => openModal('repair', item)} className="btn btn-small btn-success">Repariert</button>
									)}
									<button onClick={() => handleDelete(item)} className="btn btn-small btn-danger">Löschen</button>
								</td>
							</tr>
						))}
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

			{lightboxSrc && <Lightbox src={lightboxSrc} onClose={() => setLightboxSrc('')} />}
		</div>
	);
};

export default AdminStoragePage;