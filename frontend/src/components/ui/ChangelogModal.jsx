import React from 'react';
import Modal from './Modal';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';

const ChangelogModal = ({ changelog, onClose }) => {
	return (
		<Modal isOpen={true} onClose={onClose} title={`Was ist neu in Version ${changelog.version}?`}>
			<h3>{changelog.title}</h3>
			<p className="details-subtitle" style={{ marginTop: '-1rem' }}>
				Veröffentlicht am {new Date(changelog.releaseDate).toLocaleDateString('de-DE')}
			</p>
			<div className="markdown-content" style={{ maxHeight: '60vh', overflowY: 'auto' }}>
				<ReactMarkdown rehypePlugins={[rehypeSanitize]}>
					{changelog.notes}
				</ReactMarkdown>
			</div>
			<div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
				<button onClick={onClose} className="btn">
					Verstanden!
				</button>
			</div>
		</Modal>
	);
};

export default ChangelogModal;