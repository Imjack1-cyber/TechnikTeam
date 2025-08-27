import React from 'react';
import { SafeAreaView, StyleSheet, View } from 'react-native';

/**
 * A minimal layout component for pages that should not have the main navigation,
 * such as printable views or QR code landing pages.
 */
const MinimalLayout = ({ children }) => {
	return (
		<SafeAreaView style={styles.safeArea}>
			<View style={styles.container}>
				{children}
			</View>
		</SafeAreaView>
	);
};

const styles = StyleSheet.create({
	safeArea: {
		flex: 1,
		backgroundColor: '#f8f9fa', // bg-color
	},
	container: {
		flex: 1,
		padding: 24,
		maxWidth: 800,
		alignSelf: 'center',
		width: '100%',
	},
});


export default MinimalLayout;