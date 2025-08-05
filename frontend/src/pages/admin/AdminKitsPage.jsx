import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import useAdminData from '../../hooks/useAdminData';
import apiClient from '../../services/apiClient';
import KitModal from '../../components/admin/kits/KitModal';
import KitItemsForm from '../../components/admin/kits/KitItemsForm';
import Modal from '../../components/ui/Modal';
import QRCode from 'qrcode.react';
import { useToast } from '../../context/ToastContext';

const KitAccordion = ({ kit, onEdit, onDelete, onItemsUpdate, allStorageItems }) => {
	const [isOpen, setIsOpen] = useState(false);
	const [isQrModalOpen, setIsQrModalOpen] = useState(false);
	const packKitUrl = `${window.location.origin}/pack-kit/${kit.id}`;

	return (
		<div className="kit-container" style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '1.5rem', marginBottom: '1.5rem' }}>
			<div className="kit-header" onClick={() => setIsOpen(!isOpen)} style={{ cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
				<div>
					<h3>
						<i className={`fas ${isOpen ? 'fa-chevron-down' : 'fa-chevron-right'}`} style={{ marginRight: '0.75rem', transition: 'transform 0.2s' }}></i>
						{kit.name}
					</h3>
					<p style={{ margin: '-0.5rem 0 0 1.75rem', color: 'var(--text-muted-color)' }}>{kit.description}</p>
				</div>
				<div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }} onClick={e => e.stopPropagation()}>
					<button onClick={() => setIsQrModalOpen(true)} className="btn btn-small">QR-Code</button>
					<button onClick={() => onEdit(kit)} className="btn btn-small btn-secondary">Bearbeiten</button>
					<button onClick={() => onDelete(kit)} className="btn btn-small btn-danger">Löschen</button>
				</div>
			</div>
			{isOpen && (
				<div className="kit-content" style={{ paddingLeft: '2rem', marginTop: '1rem' }}>
					<KitItemsForm kit={kit} allStorageItems={allStorageItems} onUpdateSuccess={onItemsUpdate} />
				</div>
			)}
			<Modal isOpen={isQrModalOpen} onClose={() => setIsQrModalOpen(false)} title={`QR-Code für: ${kit.name}`}>
				<div style={{ textAlign: 'center', padding: '1rem' }}>
					<QRCode value={packKitUrl} size={256} />
					<p style={{ marginTop: '1rem' }}>Scannen, um die Packliste zu öffnen.</p>
					<a href={packKitUrl} target="_blank" rel="noopener noreferrer">{packKitUrl}</a>
				</div>
			</Modal>
		</div>
	);
};

const AdminKitsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/kits'), []);
	const { data: kits, loading, error, reload } = useApi(apiCall);
	const { storageItems } = useAdminData();
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingKit, setEditingKit] = useState(null);
	const { addToast } = useToast();

	const openModal = (kit = null) => {
		setEditingKit(kit);
		setIsModalOpen(true);
	};

	const closeModal = () => {
		setEditingKit(null);
		setIsModalOpen(false);
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	const handleDelete = async (kit) => {
		if (window.confirm(`Kit '${kit.name}' wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/kits/${kit.id}`);
				if (result.success) {
					addToast('Kit erfolgreich gelöscht.', 'success');
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error');
			}
		}
	};

	return (
		<div>
			<h1><i className="fas fa-box-open"></i> Kit-Verwaltung</h1>
			<p>Verwalten Sie hier wiederverwendbare Material-Zusammenstellungen (Kits oder Koffer).</p>

			<div className="table-controls">
				<button onClick={() => openModal()} className="btn btn-success">
					<i className="fas fa-plus"></i> Neues Kit anlegen
				</button>
			</div>

			<div className="card">
				{loading && <p>Lade Kits...</p>}
				{error && <p className="error-message">{error}</p>}
				{kits?.map(kit => (
					<KitAccordion
						key={kit.id}
						kit={kit}
						onEdit={openModal}
						onDelete={handleDelete}
						onItemsUpdate={reload}
						allStorageItems={storageItems}
					/>
				))}
			</div>

			{isModalOpen && (
				<KitModal
					isOpen={isModalOpen}
					onClose={closeModal}
					onSuccess={handleSuccess}
					kit={editingKit}
				/>
			)}
		</div>
	);
};

export default AdminKitsPage;