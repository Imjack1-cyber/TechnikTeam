import React from 'react';
import useApi from '@/hooks/useApi';
import apiClient from '@/services/apiClient';
import ProfileDetails from '@/components/profile/ProfileDetails';
import ProfileSecurity from '@/components/profile/ProfileSecurity';
import ProfileQualifications from '@/components/profile/ProfileQualifications';
import ProfileAchievements from '@/components/profile/ProfileAchievements';
import ProfileEventHistory from '@/components/profile/ProfileEventHistory';

const ProfilePage = () => {
	const { data: profileData, loading, error, reload } = useApi(() => apiClient.get('/public/profile'));

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
			<h1>Mein Profil</h1>
			<p>Hier finden Sie eine Übersicht Ihrer Daten, Qualifikationen und Aktivitäten.</p>
			<div className="responsive-dashboard-grid" id="profile-container">
				<ProfileDetails user={user} hasPendingRequest={hasPendingRequest} onUpdate={reload} />
				<ProfileSecurity passkeys={passkeys} onUpdate={reload} />
				<ProfileQualifications qualifications={qualifications} />
				<ProfileAchievements achievements={achievements} />
				<ProfileEventHistory eventHistory={eventHistory} />
			</div>
		</div>
	);
};

export default ProfilePage;