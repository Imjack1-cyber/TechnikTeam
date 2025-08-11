import React, { useState, useCallback, useMemo } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import AttendanceModal from '../../components/admin/matrix/AttendanceModal';

const AdminMatrixPage = () => {
	const apiCall = useCallback(() => apiClient.get('/matrix'), []);
	const { data, loading, error, reload } = useApi(apiCall);
	const qualificationsApiCall = useCallback(() => apiClient.get('/admin/qualifications/all'), []);
	const { data: allQualifications } = useApi(qualificationsApiCall);

	const [modalData, setModalData] = useState(null);

	const openModal = (cellData) => {
		setModalData(cellData);
	};

	const closeModal = () => {
		setModalData(null);
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	const { users, courses, meetingsByCourse, attendanceMap, completionMap } = data || {};

	const uniqueMeetingsByCourse = useMemo(() => {
		if (!meetingsByCourse) return {};
		const result = {};
		for (const courseId in meetingsByCourse) {
			const meetings = meetingsByCourse[courseId] || [];
			const nameMap = new Map();
			// Keep only the first meeting for each unique name
			for (const meeting of meetings) {
				if (!nameMap.has(meeting.name)) {
					nameMap.set(meeting.name, meeting);
				}
			}
			result[courseId] = Array.from(nameMap.values());
		}
		return result;
	}, [meetingsByCourse]);


	if (loading) return <div>Lade Matrix-Daten...</div>;
	if (error) return <div className="error-message">{error}</div>;


	return (
		<div>
			<h1><i className="fas fa-th-list"></i> Qualifikations-Matrix</h1>
			<p>Klicken Sie auf eine Zelle, um die Teilnahme an einem Meeting zu bearbeiten oder den Gesamtstatus eines Kurses für einen Benutzer zu setzen. Die Kopfzeile und die Benutzerleiste bleiben beim Scrollen fixiert.</p>

			<div className="table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th rowSpan="2" className="sticky-header sticky-col" style={{ verticalAlign: 'middle', left: 0, zIndex: 15 }}>Nutzer / Lehrgang ↓</th>
							{courses.map(course => (
								<th key={course.id} colSpan={uniqueMeetingsByCourse[course.id]?.length || 1} className="sticky-header" style={{ textAlign: 'center' }}>
									<Link to={`/admin/lehrgaenge/${course.id}/meetings`} title={`Meetings für '${course.name}' verwalten`}>
										{course.abbreviation}
									</Link>
								</th>
							))}
						</tr>
						<tr>
							{courses.map(course => {
								const meetings = uniqueMeetingsByCourse[course.id] || [];
								if (meetings.length > 0) {
									return meetings.map(meeting => (
										<th key={meeting.id} className="sticky-header" style={{ textAlign: 'center', minWidth: '120px' }}>
											<Link to={`/lehrgaenge/details/${meeting.id}`} title={`Details für '${meeting.name}' ansehen`}>
												{meeting.name}
											</Link>
										</th>
									));
								}
								// Render a single placeholder header cell to match the colSpan=1
								return <th key={`${course.id}-placeholder`} className="sticky-header" style={{ textAlign: 'center' }}>-</th>;
							})}
						</tr>
					</thead>
					<tbody>
						{users.map(user => (
							<tr key={user.id}>
								<td className="sticky-col" style={{ fontWeight: '500', left: 0 }}>{user.username}</td>
								{courses.map(course => {
									const hasCompletedCourse = completionMap[`${user.id}-${course.id}`];
									const meetings = uniqueMeetingsByCourse[course.id] || [];

									const cellBaseData = {
										userId: user.id,
										userName: user.username,
										courseId: course.id,
										courseName: course.name,
										qualification: allQualifications?.find(q => q.userId === user.id && q.courseId === course.id) || null
									};

									if (hasCompletedCourse) {
										return (
											<td
												key={`${course.id}-completed`}
												colSpan={meetings.length || 1}
												style={{ backgroundColor: 'var(--success-color)', color: '#fff', textAlign: 'center', fontWeight: 'bold', cursor: 'pointer' }}
												onClick={() => openModal({ ...cellBaseData, meetingId: 0, meetingName: '' })}
											>
												Qualifiziert
											</td>
										);
									}

									if (meetings.length === 0) {
										return <td key={`${course.id}-empty`} style={{ textAlign: 'center', cursor: 'pointer' }} onClick={() => openModal({ ...cellBaseData, meetingId: 0, meetingName: '' })}>-</td>;
									}

									return meetings.map(meeting => {
										const attendance = attendanceMap[`${user.id}-${meeting.id}`];
										const attended = attendance ? attendance.attended : false;

										const cellData = {
											...cellBaseData,
											meetingId: meeting.id,
											meetingName: meeting.name,
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
								})}
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