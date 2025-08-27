import React, { useEffect, useRef } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Modal, Animated } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome5';
// NOTE: Audio playback requires an external library like 'react-native-sound' or 'expo-av'.
// The audio logic is commented out as it's an external dependency.

const WarningNotification = ({ notification, onDismiss }) => {
	const flashAnimation = useRef(new Animated.Value(0)).current;

	useEffect(() => {
		// Play sound (requires a library)
		// const sound = new Sound('attention.mp3', Sound.MAIN_BUNDLE, (error) => {
		//   if (error) { console.log('failed to load the sound', error); return; }
		//   sound.setNumberOfLoops(-1).play();
		// });

		Animated.loop(
			Animated.sequence([
				Animated.timing(flashAnimation, { toValue: 1, duration: 500, useNativeDriver: false }),
				Animated.timing(flashAnimation, { toValue: 0, duration: 500, useNativeDriver: false }),
			])
		).start();

		return () => {
			// sound.stop().release();
			flashAnimation.stopAnimation();
		};
	}, [flashAnimation]);

	if (!notification) return null;

	const backgroundColor = flashAnimation.interpolate({
		inputRange: [0, 1],
		outputRange: ['rgba(0,0,0,0.6)', 'rgba(220, 53, 69, 0.6)'] // danger-color
	});

	return (
		<Modal
			transparent={true}
			visible={true}
			animationType="fade"
			onRequestClose={onDismiss}
		>
			<Animated.View style={[styles.overlay, { backgroundColor }]}>
				<View style={styles.modalContent}>
					<Icon name="exclamation-triangle" size={60} color="#dc3545" />
					<Text style={styles.title}>{notification.title}</Text>
					<Text style={styles.description}>{notification.description}</Text>
					<TouchableOpacity onPress={onDismiss} style={styles.button}>
						<Text style={styles.buttonText}>Verstanden</Text>
					</TouchableOpacity>
				</View>
			</Animated.View>
		</Modal>
	);
};

const styles = StyleSheet.create({
	overlay: {
		flex: 1,
		justifyContent: 'center',
		alignItems: 'center',
		padding: 20,
	},
	modalContent: {
		width: '100%',
		maxWidth: 500,
		backgroundColor: '#ffffff', // surface-color
		borderRadius: 8,
		padding: 24,
		alignItems: 'center',
		borderWidth: 3,
		borderColor: '#dc3545', // danger-color
	},
	title: {
		fontSize: 24,
		fontWeight: 'bold',
		color: '#dc3545', // danger-color
		marginTop: 16,
		marginBottom: 8,
	},
	description: {
		fontSize: 18,
		textAlign: 'center',
		marginBottom: 24,
		color: '#212529', // text-color
	},
	button: {
		backgroundColor: '#dc3545', // danger-color
		paddingVertical: 12,
		paddingHorizontal: 30,
		borderRadius: 6,
	},
	buttonText: {
		color: '#fff',
		fontSize: 16,
		fontWeight: 'bold',
	},
});

export default WarningNotification;