import React from 'react';

const ProfileAchievements = ({ achievements }) => {
	return (
		<div className="card" style={{ gridColumn: '1 / -1' }} id="profile-achievements-container">
			<h2 className="card-title">Meine Abzeichen</h2>
			{achievements.length === 0 ? (
				<p>Du hast noch keine Abzeichen verdient. Nimm an Events teil, um sie freizuschalten!</p>
			) : (
				<div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem' }}>
					{achievements.map(ach => (
						<div className="card" key={ach.id} style={{ flex: '1', minWidth: '250px', textAlign: 'center' }}>
							<i className={`fas ${ach.iconClass}`} style={{ fontSize: '3rem', color: 'var(--primary-color)', marginBottom: '1rem' }}></i>
							<h4 style={{ margin: 0 }}>{ach.name}</h4>
							<p style={{ color: 'var(--text-muted-color)', fontSize: '0.9rem' }}>{ach.description}</p>
							<small>Verdient am: {new Date(ach.earnedAt).toLocaleDateString('de-DE')}</small>
						</div>
					))}
				</div>
			)}
		</div>
	);
};

export default ProfileAchievements;