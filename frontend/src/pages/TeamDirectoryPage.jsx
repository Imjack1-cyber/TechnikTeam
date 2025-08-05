import React, { useCallback, useState } from 'react';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import Modal from '../components/ui/Modal';
import ProfileQualifications from '../components/profile/ProfileQualifications';
import ProfileAchievements from '../components/profile/ProfileAchievements';
import { useToast } from '../context/ToastContext';

const CrewCardModal = ({ isOpen, onClose, userId }) => {
	const apiCall = useCallback(() => {
		if (!userId) return null;
		return apiClient.get(`/public/profile/${userId}`);
	}, [userId]);

	const { data: profileData, loading, error } = useApi(apiCall);

	if (!isOpen) return null;

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`Crew-Karte für ${profileData?.user?.username || '...'}`}>
			{loading && <p>Lade Profildaten...</p>}
			{error && <p className="error-message">{error}</p>}
			{profileData && (
				<div className="responsive-dashboard-grid">
					<div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
						<i className={`fas ${profileData.user.profileIconClass || 'fa-user-circle'}`} style={{ fontSize: '6rem', color: 'var(--primary-color)' }}></i>
						<h2 style={{ border: 'none', padding: 0, margin: '1rem 0 0.5rem 0' }}>{profileData.user.username}</h2>
						<span className="status-badge status-info">{profileData.user.roleName}</span>
					</div>
					<ProfileQualifications qualifications={profileData.qualifications} />
					<ProfileAchievements achievements={profileData.achievements} />
				</div>
			)}
		</Modal>
	);
};

const TeamDirectoryPage = () => {
	const apiCall = useCallback(() => apiClient.get('/users'), []);
	const { data: users, loading, error } = useApi(apiCall);
	const [searchTerm, setSearchTerm] = useState('');
	const [selectedUserId, setSelectedUserId] = useState(null);

	const filteredUsers = users?.filter(user =>
		user.username.toLowerCase().includes(searchTerm.toLowerCase())
	);

	const handleViewCard = (userId) => {
		setSelectedUserId(userId);
	};

	return (
		<div>
			<h1><i className="fas fa-users"></i> Team-Verzeichnis</h1>
			<p>Hier finden Sie eine Übersicht aller Mitglieder des Technik-Teams.</p>

			<div className="card">
				<div className="form-group">
					<label htmlFor="user-search">Mitglied suchen</label>
					<input
						type="search"
						id="user-search"
						value={searchTerm}
						onChange={(e) => setSearchTerm(e.target.value)}
						placeholder="Name eingeben..."
					/>
				</div>
			</div>

			{loading && <p>Lade Mitgliederliste...</p>}
			{error && <p className="error-message">{error}</p>}

			<div className="responsive-dashboard-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))' }}>
				{filteredUsers?.map(user => (
					<div className="card" key={user.id} style={{ textAlign: 'center' }}>
						<i className={`fas ${user.profileIconClass || 'fa-user-circle'}`} style={{ fontSize: '4rem', color: 'var(--text-muted-color)' }}></i>
						<h3 style={{ margin: '1rem 0 0.5rem 0' }}>{user.username}</h3>
						<p className="details-subtitle">{user.roleName}</p>
						<button onClick={() => handleViewCard(user.id)} className="btn btn-small">
							Crew-Karte ansehen
						</button>
					</div>
				))}
			</div>

			{selectedUserId && (
				<CrewCardModal
					isOpen={!!selectedUserId}
					onClose={() => setSelectedUserId(null)}
					userId={selectedUserId}
				/>
			)}
		</div>
	);
};

export default TeamDirectoryPage;