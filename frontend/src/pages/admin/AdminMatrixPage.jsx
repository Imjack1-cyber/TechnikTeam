import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import AttendanceModal from '../../components/admin/matrix/AttendanceModal';

const AdminMatrixPage = () => {
	const { data, loading, error, reload } = useApi(() => apiClient.get('/matrix'));
	const [modalData, setModalData] = useState(null);

	const openModal = (cellData) => {
		setModalData(cellData);
	};

	const closeModal = () => {
		setModalData(null);
	};

	const handleSuccess = () => {
		closeModal();
		reload(); // Reload all data to reflect changes
	};

	if (loading) return <div>Lade Matrix-Daten...</div>;
	if (error) return <div className="error-message">{error}</div>;

	const { users, courses, meetingsByCourse, attendanceMap } = data;

	return (
		<div>
			<h1><i className="fas fa-th-list"></i> Qualifikations-Matrix</h1>
			<p>Klicken Sie auf eine Zelle, um die Teilnahme an einem Meeting zu bearbeiten. Die Kopfzeile und die Benutzerleiste bleiben beim Scrollen fixiert.</p>

			<div className="table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th rowSpan="2" className="sticky-header sticky-col" style={{ verticalAlign: 'middle', left: 0, zIndex: 15 }}>Nutzer / Lehrgang ↓</th>
							{courses.map(course => (
								<th key={course.id} colSpan={meetingsByCourse[course.id]?.length || 1} className="sticky-header" style={{ textAlign: 'center' }}>
									<Link to={`/admin/lehrgaenge/${course.id}/meetings`} title={`Meetings für '${course.name}' verwalten`}>
										{course.abbreviation}
									</Link>
								</th>
							))}
						</tr>
						<tr>
							{courses.map(course => (
								(meetingsByCourse[course.id] || []).map(meeting => (
									<th key={meeting.id} className="sticky-header" style={{ textAlign: 'center', minWidth: '120px' }}>
										{meeting.name}
									</th>
								))
							))}
						</tr>
					</thead>
					<tbody>
						{users.map(user => (
							<tr key={user.id}>
								<td className="sticky-col" style={{ fontWeight: '500', left: 0 }}>{user.username}</td>
								{courses.map(course => (
									(meetingsByCourse[course.id] || []).map(meeting => {
										const attendance = attendanceMap[`${user.id}-${meeting.id}`];
										const attended = attendance ? attendance.attended : false;

										const cellData = {
											userId: user.id,
											userName: user.username,
											meetingId: meeting.id,
											meetingName: `${course.name} - ${meeting.name}`,
											attended: attended,
											remarks: attendance?.remarks || ''
										};

										return (
											<td
												key={meeting.id}
												className="qual-cell"
												onClick={() => openModal(cellData)}
												style={{ textAlign: 'center', fontWeight: 'bold', cursor: 'pointer' }}
												title="Klicken zum Bearbeiten"
											>
												{attended ?
													<span style={{ fontSize: '1.2rem', color: 'var(--success-color)' }}>✔</span> :
													<span className="text-muted">-</span>
												}
											</td>
										);
									})
								))}
							</tr>
						))}
					</tbody>
				</table>
			</div>

			{modalData && (
				<AttendanceModal
					isOpen={!!modalData}
					onClose={closeModal}
					onSuccess={handleSuccess}
					cellData={modalData}
				/>
			)}
		</div>
	);
};

export default AdminMatrixPage;