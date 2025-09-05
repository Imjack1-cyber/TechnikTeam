import React, { useEffect, useRef } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Modal, Animated, Platform } from 'react-native';
import Icon from '@expo/vector-icons/FontAwesome5';
// TODO: To enable sound, the 'expo-av' package must be installed.
// Run 'npx expo install expo-av' in the /frontend directory.
// Then, uncomment the line below and the audio-related code in the useEffect hook.
// import { Audio } from 'expo-av';

const WarningNotification = ({ notification, onDismiss }) => {
	const flashAnimation = useRef(new Animated.Value(0)).current;
    // const soundObject = useRef(new Audio.Sound()); // UNCOMMENT AFTER INSTALL

	useEffect(() => {
        let isMounted = true;
        /* UNCOMMENT THIS BLOCK AFTER INSTALLING expo-av
        const loadAndPlaySound = async () => {
            try {
                await Audio.setAudioModeAsync({ playsInSilentModeIOS: true });
                await soundObject.current.loadAsync(require('../../../assets/audio/attention.mp3'));
                await soundObject.current.setIsLoopingAsync(true);
                if(isMounted) {
                    await soundObject.current.playAsync();
                }
            } catch (error) {
                console.error("Failed to load and play sound for warning notification:", error);
            }
        };

		loadAndPlaySound();
        */

		Animated.loop(
			Animated.sequence([
				Animated.timing(flashAnimation, { toValue: 1, duration: 500, useNativeDriver: false }),
				Animated.timing(flashAnimation, { toValue: 0, duration: 500, useNativeDriver: false }),
			])
		).start();

		return () => {
            isMounted = false;
			// soundObject.current.unloadAsync(); // UNCOMMENT AFTER INSTALL
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