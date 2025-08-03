import React, { useMemo } from 'react';
import { Link, useLocation } from 'react-router-dom';
import useTypingAnimation from '../../hooks/useTypingAnimation';

const NotFoundPage = () => {
	const location = useLocation();
	const path = location.pathname;

	// useMemo ensures the 'lines' array is not re-created on every render
	const lines = useMemo(() => [
		{ text: `Führe Befehl aus: find . -name "${path}"`, className: 'info', delayAfter: 800 },
		{ text: `find: '${path}': Datei oder Verzeichnis nicht gefunden`, className: 'warn', delayAfter: 500 },
		{ text: 'FEHLER 404: Ressource nicht gefunden.', className: 'fail' },
		{ text: 'Vorschlag: Die angeforderte Ressource ist nicht verfügbar. Versuchen Sie, zum Dashboard zurückzukehren.', className: 'info' },
		{ text: `Führe aus: cd /home`, className: 'info' },
	], [path]);

	const { containerRef, renderedLines, isComplete } = useTypingAnimation(lines);

	return (
		<div className="terminal">
			<div className="terminal-header">
				<span className="red"></span>
				<span className="yellow"></span>
				<span className="green"></span>
				<div className="title">bash</div>
			</div>
			<div className="terminal-body" ref={containerRef}>
				{renderedLines.map((line, index) => (
					<div key={index} className={`terminal-line ${line.className}`}>
						<span className="terminal-prompt">{index < 1 ? '$' : '>'}</span>
						<span>{line.text}</span>
						{index === renderedLines.length - 1 && !isComplete && <span className="cursor"></span>}
					</div>
				))}
			</div>
			<Link to="/home" className={`btn ${isComplete ? 'visible' : ''}`}>
				<i className="fas fa-home"></i> Zum Dashboard
			</Link>
		</div>
	);
};

export default NotFoundPage;