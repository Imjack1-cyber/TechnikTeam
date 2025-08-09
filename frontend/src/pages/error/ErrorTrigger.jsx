import { useEffect } from 'react';

const ErrorTrigger = () => {
	useEffect(() => {
		// This will cause a rendering error that the boundary will catch.
		throw new Error("Dies ist ein simulierter Rendering-Fehler zum Testen der 500-Seite.");
	}, []);

	return <div>You should not see this.</div>;
};

export default ErrorTrigger;