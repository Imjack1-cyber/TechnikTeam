import React, { useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import ProfileQualifications from '../components/profile/ProfileQualifications';
import ProfileAchievements from '../components/profile/ProfileAchievements';
import ProfileEventHistory from '../components/profile/ProfileEventHistory';

const UserProfilePage = () => {
	const { userId } = useParams();
	const apiCall = useCallback(() => apiClient.get(`/public/profile/${userId}`), [userId]);
	const { data: profileData, loading, error } = useApi(apiCall);

	if (loading) {
		return <div>Lade Benutzerprofil...</div>;
	}

	if (error) {
		return <div className="error-message">{error}</div>;
	}

	if (!profileData) {
		return <div>Keine Profildaten für diesen Benutzer gefunden.</div>;
	}

	const { user, eventHistory, qualifications, achievements } = profileData;

	return (
		<div>
			<div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
				<i className={`fas ${user.profileIconClass || 'fa-user-circle'}`} style={{ fontSize: '80px', color: 'var(--text-muted-color)', width: '80px', textAlign: 'center' }}></i>
				<div>
					<h1><i className="fas fa-id-card"></i> Crew-Karte: {user.username}</h1>
					<p className="details-subtitle" style={{ marginTop: '-1rem' }}>
						{user.roleName}
					</p>
				</div>
			</div>

			<div className="responsive-dashboard-grid">
				<ProfileQualifications qualifications={qualifications} />
				<ProfileAchievements achievements={achievements} />
				<ProfileEventHistory eventHistory={eventHistory} />
			</div>

			<Link to="/team" className="btn btn-secondary" style={{ marginTop: '1.5rem' }}>
				<i className="fas fa-arrow-left"></i> Zurück zum Team-Verzeichnis
			</Link>
		</div>
	);
};

export default UserProfilePage;