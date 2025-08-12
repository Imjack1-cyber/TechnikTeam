import React, { useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';

const TeamDirectoryPage = () => {
	const apiCall = useCallback(() => apiClient.get('/users'), []);
	const { data: users, loading, error } = useApi(apiCall);
	const [searchTerm, setSearchTerm] = useState('');

	const filteredUsers = users?.filter(user =>
		user.username.toLowerCase().includes(searchTerm.toLowerCase())
	);

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
						<Link to={`/team/${user.id}`} className="btn btn-small">
							Profil ansehen
						</Link>
					</div>
				))}
			</div>
		</div>
	);
};

export default TeamDirectoryPage;