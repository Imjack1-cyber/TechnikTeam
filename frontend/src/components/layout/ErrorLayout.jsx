import React from 'react';
import { View, StyleSheet, SafeAreaView, StatusBar } from 'react-native';
import { useAuthStore } from '../../store/authStore';

// Note: In React Native, theming is handled differently.
// We'll apply basic styles here, but a full theme would use a ThemeContext.
// This component ensures a safe area and consistent background for error screens.

const ErrorLayout = ({ children }) => {
	const theme = useAuthStore.getState().theme;
	const isDarkMode = theme === 'dark';

	const containerStyle = {
		backgroundColor: isDarkMode ? '#0d1117' : '#f8f9fa', // bg-color
	};

	return (
		<SafeAreaView style={[styles.safeArea, containerStyle]}>
			<StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
			<View style={styles.wrapper}>
				{children}
			</View>
		</SafeAreaView>
	);
};

const styles = StyleSheet.create({
	safeArea: {
		flex: 1,
	},
	wrapper: {
		flex: 1,
		justifyContent: 'center',
		alignItems: 'center',
		padding: 16,
		fontFamily: 'Courier New', // Note: custom fonts need to be added to the project
	},
});


export default ErrorLayout;