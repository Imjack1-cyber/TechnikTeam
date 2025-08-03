import React, { useMemo } from 'react';
import { useRouteError, Link } from 'react-router-dom';
import useTypingAnimation from '../../hooks/useTypingAnimation';

const ErrorPage = () => {
	const error = useRouteError();
	console.error(error);

	// Safely determine the error message to display
	const errorMessage = useMemo(() => {
		if (typeof error === 'string') return error;
		if (error instanceof Error) return error.message;
		if (error?.statusText) return error.statusText;
		if (error?.message) return error.message;
		return "Ein unbekannter Fehler ist aufgetreten.";
	}, [error]);

	// useMemo ensures the 'lines' array is not re-created on every render
	const lines = useMemo(() => [
		{ text: 'SYSTEMDIAGNOSE WIRD GESTARTET...', className: 'info' },
		{ text: 'Speichermodule werden gescannt...', className: 'info', delayAfter: 500 },
		{ text: '[OK] Speicherintegritätsprüfung bestanden.', className: 'ok' },
		{ text: 'Anwendungsstatus wird überprüft...', className: 'info', delayAfter: 500 },
		{ text: `[FEHLER] Unbehandelte Ausnahme erkannt: ${errorMessage}`, className: 'fail', delayAfter: 800 },
		{ text: 'FEHLER 500: Interner Serverfehler.', className: 'fail' },
		{ text: 'Ein kritischer Fehler ist bei der Verarbeitung der Anfrage aufgetreten.', className: 'info' },
		{ text: 'Der Systemadministrator wurde benachrichtigt.', className: 'info' },
		{ text: 'Wiederherstellungsoptionen werden vorbereitet...', className: 'warn' },
	], [errorMessage]);

	const { containerRef, renderedLines, isComplete } = useTypingAnimation(lines);

	return (
		<div className="terminal">
			<div className="terminal-header">
				<span className="red"></span>
				<span className="yellow"></span>
				<span className="green"></span>
				<div className="title">SYSTEM_DIAGNOSTIC.LOG</div>
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
			<Link to="/home" className={`btn ${isComplete ? 'visible' : ''}`}>
				<i className="fas fa-home"></i> Zum Dashboard
			</Link>
		</div>
	);
};

export default ErrorPage;