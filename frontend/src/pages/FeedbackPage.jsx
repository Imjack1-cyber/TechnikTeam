import React, { useState } from 'react';
import useApi from '@/hooks/useApi';
import apiClient from '@/services/apiClient';
import StatusBadge from '@/components/ui/StatusBadge';

const FeedbackPage = () => {
	const { data: submissions, loading, error, reload } = useApi(() => apiClient.get('/public/feedback/user'));
	const [subject, setSubject] = useState('');
	const [content, setContent] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [submitError, setSubmitError] = useState('');
	const [submitSuccess, setSubmitSuccess] = useState('');

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setSubmitError('');
		setSubmitSuccess('');

		try {
			const result = await apiClient.post('/public/feedback/general', { subject, content });
			if (result.success) {
				setSubmitSuccess('Vielen Dank! Dein Feedback wurde erfolgreich übermittelt.');
				setSubject('');
				setContent('');
				reload(); // Reload the list of submissions
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setSubmitError(err.message || 'Senden fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<div>
			<h1>Feedback & Wünsche</h1>
			<div className="responsive-dashboard-grid">
				<div className="card">
					<h2 className="card-title">Neues Feedback einreichen</h2>
					<p>Hast du eine Idee, einen Verbesserungsvorschlag oder ist dir ein Fehler aufgefallen?</p>

					{submitSuccess && <p className="success-message">{submitSuccess}</p>}
					{submitError && <p className="error-message">{submitError}</p>}

					<form onSubmit={handleSubmit}>
						<div className="form-group">
							<label htmlFor="subject">Betreff</label>
							<input type="text" id="subject" value={subject} onChange={(e) => setSubject(e.target.value)} required maxLength="255" placeholder="z.B. Feature-Wunsch: Dunkelmodus" />
						</div>
						<div className="form-group">
							<label htmlFor="content">Deine Nachricht</label>
							<textarea id="content" value={content} onChange={(e) => setContent(e.target.value)} rows="8" required placeholder="Bitte beschreibe deine Idee oder das Problem..."></textarea>
						</div>
						<button type="submit" className="btn btn-success" disabled={isSubmitting}>
							{isSubmitting ? <><i className="fas fa-spinner fa-spin"></i> Senden...</> : <><i className="fas fa-paper-plane"></i> Feedback absenden</>}
						</button>
					</form>
				</div>
				<div className="card">
					<h2 className="card-title">Mein eingereichtes Feedback</h2>
					{loading && <p>Lade Feedback...</p>}
					{error && <p className="error-message">{error}</p>}
					{submissions && submissions.length === 0 && <p>Sie haben noch kein Feedback eingereicht.</p>}
					{submissions && submissions.length > 0 && (
						<div style={{ maxHeight: '500px', overflowY: 'auto' }}>
							{submissions.map(sub => (
								<div className="list-item-card" key={sub.id} style={{ marginBottom: '1rem' }}>
									<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
										<h3 className="card-title" style={{ border: 'none', padding: 0 }}>{sub.subject}</h3>
										<StatusBadge status={sub.status} />
									</div>
									<p style={{ color: 'var(--text-muted-color)', marginTop: '-0.75rem', marginBottom: '1rem' }}>
										Eingereicht am {new Date(sub.submittedAt).toLocaleString('de-DE')}
									</p>
									<p>{sub.content}</p>
								</div>
							))}
						</div>
					)}
				</div>
			</div>
		</div>
	);
};

export default FeedbackPage;