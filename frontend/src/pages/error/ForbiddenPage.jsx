import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import useTypingAnimation from '../../hooks/useTypingAnimation';
import { useAuthStore } from '../../store/authStore';

const ForbiddenPage = () => {
	const user = useAuthStore(state => state.user);

	const lines = useMemo(() => [
		{ text: 'Attempting to access restricted area...', className: 'info' },
		{ text: `Authenticating user: ${user?.username || 'GUEST'}`, className: 'info', delayAfter: 500 },
		{ text: 'Checking clearance level...', className: 'info', delayAfter: 800 },
		{ text: '[ACCESS DENIED]', className: 'fail', speed: 80 },
		{ text: 'ERROR 403: Insufficient permissions.', className: 'fail' },
		{ text: 'Your current role does not grant access to this resource.', className: 'warn' },
		{ text: 'This attempt has been logged.', className: 'info' },
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
					<i className="fas fa-arrow-left"></i> Return to Safety
				</Link>
			</div>
		</div>
	);
};

export default ForbiddenPage;