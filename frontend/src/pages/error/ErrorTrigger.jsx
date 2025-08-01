import { useEffect } from 'react';

const ErrorTrigger = () => {
	useEffect(() => {
		// This will cause a rendering error that the boundary will catch.
		throw new Error("This is a simulated rendering error for testing the 500 page.");
	}, []);

	return <div>You should not see this.</div>;
};

export default ErrorTrigger;