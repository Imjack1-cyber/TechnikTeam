import React, { useState } from 'react';
import useApi from '@/hooks/useApi';
import useAdminData from '@/hooks/useAdminData';
import apiClient from '@/services/apiClient';
import KitModal from '@/components/admin/kits/KitModal';
import KitItemsForm from '@/components/admin/kits/KitItemsForm';
import Modal from '@/components/ui/Modal';
import QRCode from 'qrcode.react';

const KitAccordion = ({ kit, onEdit, onDelete, onItemsUpdate, allStorageItems }) => {
	const [isOpen, setIsOpen] = useState(false);
	const [isQrModalOpen, setIsQrModalOpen] = useState(false);
	const packKitUrl = `${window.location.origin}/pack-kit/${kit.id}`;

	return (
		<div className="kit-container" style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '1.5rem', marginBottom: '1.5rem' }}>
			<div className="kit-header" onClick={() => setIsOpen(!isOpen)}>
				<div>
					<h3>
						<i className={`fas ${isOpen ? 'fa-chevron-up' : 'fa-chevron-down'} toggle-icon`}></i> {kit.name}
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
				</div>
			</Modal>
		</div>
	);
};

const AdminKitsPage = () => {
	const { data: kits, loading, error, reload } = useApi(() => apiClient.get('/kits'));
	const { storageItems, loading: itemsLoading } = useAdminData();
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingKit, setEditingKit] = useState(null);

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
				await apiClient.delete(`/kits/${kit.id}`);
				reload();
			} catch (err) {
				alert(`Error: ${err.message}`);
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