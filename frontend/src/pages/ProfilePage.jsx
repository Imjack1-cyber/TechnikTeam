import React, { useCallback } from 'react';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import ProfileDetails from '../components/profile/ProfileDetails';
import ProfileSecurity from '../components/profile/ProfileSecurity';
import ProfileQualifications from '../components/profile/ProfileQualifications';
import ProfileAchievements from '../components/profile/ProfileAchievements';
import ProfileEventHistory from '../components/profile/ProfileEventHistory';
import { useToast } from '../context/ToastContext';
import { Link } from 'react-router-dom';

const ProfilePage = () => {
	const { addToast } = useToast();
	const apiCall = useCallback(() => apiClient.get('/public/profile'), []);
	const { data: profileData, loading, error, reload } = useApi(apiCall);

	const handleUpdate = () => {
		addToast('Profildaten aktualisiert', 'success');
		reload();
	};

	if (loading) {
		return (
			<div>
				<h1>Mein Profil</h1>
				<p>Lade Profildaten...</p>
			</div>
		);
	}

	if (error) {
		return <div className="error-message">{error}</div>;
	}

	if (!profileData) {
		return <div>Keine Profildaten gefunden.</div>;
	}

	const { user, eventHistory, qualifications, achievements, passkeys, hasPendingRequest } = profileData;

	return (
		<div>
			<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
				<h1><i className="fas fa-user-circle"></i> Mein Profil</h1>
				<Link to="/profil/einstellungen" className="btn btn-secondary">
					<i className="fas fa-cog"></i> Layout-Einstellungen
				</Link>
			</div>
			<p>Hier finden Sie eine Übersicht Ihrer Daten, Qualifikationen und Aktivitäten.</p>
			<div className="responsive-dashboard-grid" id="profile-container">
				<ProfileDetails user={user} hasPendingRequest={hasPendingRequest} onUpdate={handleUpdate} />
				<ProfileSecurity passkeys={passkeys} onUpdate={handleUpdate} />
				<ProfileQualifications qualifications={qualifications} />
				<ProfileAchievements achievements={achievements} />
				<ProfileEventHistory eventHistory={eventHistory} />
			</div>
		</div>
	);
};

export default ProfilePage;