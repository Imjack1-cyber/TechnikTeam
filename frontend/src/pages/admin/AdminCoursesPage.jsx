import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import useApi from '@/hooks/useApi';
import apiClient from '@/services/apiClient';
import Modal from '@/components/ui/Modal';

const AdminCoursesPage = () => {
	const { data: courses, loading, error, reload } = useApi(() => apiClient.get('/courses'));
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingCourse, setEditingCourse] = useState(null);
	const [formError, setFormError] = useState('');

	const handleOpenNewModal = () => {
		setEditingCourse(null);
		setIsModalOpen(true);
	};

	const handleOpenEditModal = (course) => {
		setEditingCourse(course);
		setIsModalOpen(true);
	};

	const handleCloseModal = () => {
		setIsModalOpen(false);
		setEditingCourse(null);
		setFormError('');
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		const formData = new FormData(e.target);
		const data = Object.fromEntries(formData.entries());

		try {
			const result = editingCourse
				? await apiClient.put(`/courses/${editingCourse.id}`, data)
				: await apiClient.post('/courses', data);

			if (result.success) {
				handleCloseModal();
				reload();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setFormError(err.message || 'Ein Fehler ist aufgetreten.');
		}
	};

	const handleDelete = async (course) => {
		if (window.confirm(`Vorlage '${course.name}' wirklich löschen? Alle zugehörigen Meetings und Qualifikationen werden auch gelöscht!`)) {
			try {
				await apiClient.delete(`/courses/${course.id}`);
				reload();
			} catch (err) {
				alert(`Fehler: ${err.message}`);
			}
		}
	};

	return (
		<div>
			<h1>Lehrgangs-Vorlagen verwalten</h1>
			<p>Dies sind die übergeordneten Lehrgänge. Einzelne Termine (Meetings) werden für jede Vorlage separat verwaltet.</p>

			<div className="table-controls">
				<button onClick={handleOpenNewModal} className="btn btn-success">
					<i className="fas fa-plus"></i> Neue Vorlage
				</button>
			</div>

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Name der Vorlage</th>
							<th>Abkürzung (für Matrix)</th>
							<th style={{ minWidth: '350px' }}>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{loading && <tr><td colSpan="3">Lade Vorlagen...</td></tr>}
						{error && <tr><td colSpan="3" className="error-message">{error}</td></tr>}
						{courses?.map(course => (
							<tr key={course.id}>
								<td>{course.name}</td>
								<td>{course.abbreviation}</td>
								<td style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
									<Link to={`/admin/lehrgaenge/${course.id}/meetings`} className="btn btn-small">
										<i className="fas fa-calendar-day"></i> Meetings
									</Link>
									<button onClick={() => handleOpenEditModal(course)} className="btn btn-small btn-secondary">
										<i className="fas fa-edit"></i> Bearbeiten
									</button>
									<button onClick={() => handleDelete(course)} className="btn btn-small btn-danger">
										<i className="fas fa-trash"></i> Löschen
									</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>

			{isModalOpen && (
				<Modal isOpen={isModalOpen} onClose={handleCloseModal} title={editingCourse ? "Lehrgangs-Vorlage bearbeiten" : "Neue Lehrgangs-Vorlage anlegen"}>
					<form onSubmit={handleSubmit}>
						{formError && <p className="error-message">{formError}</p>}
						<div className="form-group">
							<label htmlFor="name-modal">Name der Vorlage</label>
							<input type="text" id="name-modal" name="name" defaultValue={editingCourse?.name} required />
						</div>
						<div className="form-group">
							<label htmlFor="abbreviation-modal">Abkürzung (max. 10 Zeichen)</label>
							<input type="text" id="abbreviation-modal" name="abbreviation" defaultValue={editingCourse?.abbreviation} maxLength="10" required />
						</div>
						<div className="form-group">
							<label htmlFor="description-modal">Allgemeine Beschreibung</label>
							<textarea id="description-modal" name="description" defaultValue={editingCourse?.description} rows="4"></textarea>
						</div>
						<button type="submit" className="btn">
							<i className="fas fa-save"></i> Vorlage Speichern
						</button>
					</form>
				</Modal>
			)}
		</div>
	);
};

export default AdminCoursesPage;