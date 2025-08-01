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
		return "An unknown error occurred.";
	}, [error]);

	// useMemo ensures the 'lines' array is not re-created on every render
	const lines = useMemo(() => [
		{ text: 'INITIATING SYSTEM DIAGNOSTIC...', className: 'info' },
		{ text: 'Scanning memory modules...', className: 'info', delayAfter: 500 },
		{ text: '[OK] Memory integrity check passed.', className: 'ok' },
		{ text: 'Checking application state...', className: 'info', delayAfter: 500 },
		{ text: `[FAIL] Unhandled exception detected: ${errorMessage}`, className: 'fail', delayAfter: 800 },
		{ text: 'ERROR 500: Internal Server Error.', className: 'fail' },
		{ text: 'A critical error occurred while processing the request.', className: 'info' },
		{ text: 'The system administrator has been notified.', className: 'info' },
		{ text: 'Preparing recovery options...', className: 'warn' },
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
				<i className="fas fa-home"></i> Go to Dashboard
			</Link>
		</div>
	);
};

export default ErrorPage;