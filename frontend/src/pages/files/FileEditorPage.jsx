import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';
import { useAuthStore } from '../../store/authStore';

const FileEditorPage = () => {
	const { fileId } = useParams();
	const { addToast } = useToast();
	const navigate = useNavigate();
	const { isAdmin } = useAuthStore();
	const [content, setContent] = useState('');
	const [initialContent, setInitialContent] = useState(null);
	const [viewMode, setViewMode] = useState('edit');
	const [saveStatus, setSaveStatus] = useState('idle');
	const [error, setError] = useState('');
	const saveTimer = useRef(null);

	const contentRef = useRef(content);
	useEffect(() => {
		contentRef.current = content;
	}, [content]);

	const apiCall = useCallback(() => {
		// Use the correct endpoint based on whether the user is an admin or not
		const endpoint = isAdmin ? `/admin/files/content/${fileId}` : `/public/files/content/${fileId}`;
		return apiClient.get(endpoint);
	}, [fileId, isAdmin]);

	const { data: fileData, loading, error: fetchError } = useApi(apiCall);

	useEffect(() => {
		if (fileData?.content !== null && fileData?.content !== undefined) {
			setContent(fileData.content);
			setInitialContent(fileData.content);
		}
	}, [fileData]);

	const handleSave = useCallback(async () => {
		if (saveStatus === 'saving' || contentRef.current === initialContent) {
			return;
		}

		setSaveStatus('saving');
		setError('');
		try {
			const endpoint = isAdmin ? `/admin/files/content/${fileId}` : `/public/files/content/${fileId}`;
			const result = await apiClient.put(endpoint, { content: contentRef.current });
			if (result.success) {
				setInitialContent(contentRef.current);
				setSaveStatus('saved');
				setTimeout(() => setSaveStatus('idle'), 2000);
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Speichern fehlgeschlagen.');
			setSaveStatus('idle');
			addToast(`Fehler beim Speichern: ${err.message}`, 'error');
		}
	}, [fileId, initialContent, saveStatus, isAdmin, addToast]);

	// Auto-save logic using setInterval (throttling)
	useEffect(() => {
		const interval = setInterval(() => {
			handleSave();
		}, 5000); // Attempt to save every 5 seconds

		return () => clearInterval(interval); // Cleanup on unmount
	}, [handleSave]);


	const getSaveButtonContent = () => {
		switch (saveStatus) {
			case 'saving':
				return <><i className="fas fa-spinner fa-spin"></i> Speichern...</>;
			case 'saved':
				return <><i className="fas fa-check"></i> Gespeichert!</>;
			default:
				return 'Jetzt Speichern';
		}
	};

	if (loading) return <div>Lade Dateiinhalt...</div>;
	if (fetchError) return <div className="error-message">{fetchError}</div>;

	const backUrl = isAdmin ? "/admin/content/dateien" : "/dateien";

	return (
		<div>
			<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '1rem' }}>
				<h1><i className="fas fa-pen-alt"></i> Editor: {fileData?.filename}</h1>
				<div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
					<div className="modal-tabs">
						<button className={`modal-tab-button ${viewMode === 'edit' ? 'active' : ''}`} onClick={() => setViewMode('edit')}>
							<i className="fas fa-edit"></i> Bearbeiten
						</button>
						<button className={`modal-tab-button ${viewMode === 'preview' ? 'active' : ''}`} onClick={() => setViewMode('preview')}>
							<i className="fas fa-eye"></i> Vorschau
						</button>
					</div>
					<Link to={backUrl} className="btn btn-secondary">Zurück</Link>
					<button onClick={handleSave} className={`btn ${saveStatus === 'saved' ? 'btn-success' : ''}`} disabled={saveStatus === 'saving' || content === initialContent}>
						{getSaveButtonContent()}
					</button>
				</div>
			</div>
			{error && <p className="error-message">{error}</p>}
			<div className="card" style={{ height: '70vh', display: 'flex', flexDirection: 'column' }}>
				{viewMode === 'edit' ? (
					<textarea
						value={content}
						onChange={(e) => setContent(e.target.value)}
						style={{ flexGrow: 1, width: '100%', fontFamily: 'monospace', resize: 'none', border: 'none', padding: 0 }}
						className="form-group"
					/>
				) : (
					<div className="markdown-content" style={{ flexGrow: 1, overflowY: 'auto' }}>
						<ReactMarkdown rehypePlugins={[rehypeSanitize]}>{content}</ReactMarkdown>
					</div>
				)}
			</div>
		</div>
	);
};

export default FileEditorPage;