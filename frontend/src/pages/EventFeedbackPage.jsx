import React, { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';

const EventFeedbackPage = () => {
	const { eventId } = useParams();
	const navigate = useNavigate();

	const [rating, setRating] = useState(0);
	const [comments, setComments] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	const { data, loading, error: fetchError } = useApi(() => apiClient.get(`/public/feedback/forms?eventId=${eventId}`));

	const handleSubmit = async (e) => {
		e.preventDefault();
		if (rating === 0) {
			setError('Bitte wählen Sie eine Sternebewertung aus.');
			return;
		}
		setIsSubmitting(true);
		setError('');

		try {
			const result = await apiClient.post('/public/feedback/event', {
				formId: data.form.id,
				rating,
				comments
			});
			if (result.success) {
				navigate('/profil');
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Feedback konnte nicht übermittelt werden.');
		} finally {
			setIsSubmitting(false);
		}
	};

	if (loading) return <div>Lade Feedback-Formular...</div>;
	if (fetchError) return <div className="error-message">{fetchError}</div>;
	if (!data) return <div className="error-message">Formulardaten nicht gefunden.</div>;

	if (data.alreadySubmitted) {
		return (
			<div className="card" style={{ maxWidth: '700px', margin: 'auto' }}>
				<h1>Feedback bereits abgegeben</h1>
				<p className="info-message">Vielen Dank, du hast bereits Feedback für das Event "{data.event.name}" abgegeben.</p>
				<Link to="/profil" className="btn">Zurück zum Profil</Link>
			</div>
		);
	}

	return (
		<div style={{ maxWidth: '700px', margin: 'auto' }}>
			<div className="card">
				<h1>Feedback für: {data.event.name}</h1>
				<p>Dein Feedback hilft uns, zukünftige Events zu verbessern.</p>
				{error && <p className="error-message">{error}</p>}
				<form onSubmit={handleSubmit}>
					<div className="form-group">
						<label>Gesamteindruck (1 = schlecht, 5 = super)</label>
						<div className="star-rating">
							{[5, 4, 3, 2, 1].map(star => (
								<React.Fragment key={star}>
									<input type="radio" id={`star${star}`} name="rating" value={star} onChange={() => setRating(star)} checked={rating === star} />
									<label htmlFor={`star${star}`} title={`${star} Sterne`}></label>
								</React.Fragment>
							))}
						</div>
					</div>
					<div className="form-group">
						<label htmlFor="comments">Kommentare & Verbesserungsvorschläge</label>
						<textarea id="comments" name="comments" value={comments} onChange={(e) => setComments(e.target.value)} rows="5"></textarea>
					</div>
					<button type="submit" className="btn" disabled={isSubmitting}>
						{isSubmitting ? 'Wird gesendet...' : 'Feedback absenden'}
					</button>
				</form>
			</div>
		</div>
	);
};

export default EventFeedbackPage; 