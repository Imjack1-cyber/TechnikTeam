import { useEffect } from 'react';
import { View } from 'react-native';

const ErrorTrigger = () => {
	useEffect(() => {
		// This will cause a rendering error that the boundary will catch.
		throw new Error("Dies ist ein simulierter Rendering-Fehler zum Testen der 500-Seite.");
	}, []);

	return <View />; // Must return a valid component
};

export default ErrorTrigger;