import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import useTypingAnimation from '../../hooks/useTypingAnimation';
import { useAuthStore } from '../../store/authStore';

const ForbiddenPage = () => {
	const user = useAuthStore(state => state.user);

	const lines = useMemo(() => [
		{ text: 'Zugriffsversuch auf geschützten Bereich...', className: 'info' },
		{ text: `Benutzer wird authentifiziert: ${user?.username || 'GAST'}`, className: 'info', delayAfter: 500 },
		{ text: 'Berechtigungsstufe wird geprüft...', className: 'info', delayAfter: 800 },
		{ text: '[ZUGRIFF VERWEIGERT]', className: 'fail', speed: 80 },
		{ text: 'FEHLER 403: Unzureichende Berechtigungen.', className: 'fail' },
		{ text: 'Ihre aktuelle Rolle gewährt keinen Zugriff auf diese Ressource.', className: 'warn' },
		{ text: 'Dieser Versuch wurde protokolliert.', className: 'info' },
	], [user]);

	const { containerRef, renderedLines, isComplete } = useTypingAnimation(lines);

	return (
		<div className="full-screen-terminal">
			<div className="terminal-header">
				<span className="red"></span>
				<span className="yellow"></span>
				<span className="green"></span>
				<div className="title">SECURITY.LOG</div>
			</div>
			<div className="terminal-body" ref={containerRef}>
				{renderedLines.map((line, index) => (
					<div key={index} className={`terminal-line ${line.className}`}>
						<span className="terminal-prompt">{'>'}</span>
						<span>{line.text}</span>
						{index === renderedLines.length - 1 && !isComplete && <span className="cursor"></span>}
					</div>
				))}
			</div>
			<div className="terminal-footer">
				<Link to="/home" className={`btn ${isComplete ? 'visible' : ''}`}>
					<i className="fas fa-arrow-left"></i> Zurück zum sicheren Bereich
				</Link>
			</div>
		</div>
	);
};

export default ForbiddenPage;