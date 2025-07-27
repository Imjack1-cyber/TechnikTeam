import React from 'react';

const ProfileQualifications = ({ qualifications }) => {
	return (
		<div className="card" id="profile-qualifications-container">
			<h2 className="card-title">Meine Qualifikationen</h2>
			<div className="table-wrapper" style={{ maxHeight: '400px', overflowY: 'auto' }}>
				<table className="data-table">
					<thead>
						<tr>
							<th>Lehrgang</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						{qualifications.length === 0 ? (
							<tr><td colSpan="2">Keine Qualifikationen erworben.</td></tr>
						) : (
							qualifications.map(qual => (
								<tr key={qual.courseId}>
									<td>{qual.courseName}</td>
									<td>{qual.status}</td>
								</tr>
							))
						)}
					</tbody>
				</table>
			</div>
		</div>
	);
};

export default ProfileQualifications;