import React, { useEffect, useRef } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Modal, Animated, Platform } from 'react-native';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing } from '../../styles/theme';
// TODO: To enable sound, the 'expo-av' package must be installed.
// Run 'npx expo install expo-av' in the /frontend directory.
// Then, uncomment the line below and the audio-related code in the useEffect hook.
import { Audio } from 'expo-av';

const WarningNotification = ({ notification, onDismiss }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);
	const flashAnimation = useRef(new Animated.Value(0)).current;
    const soundObject = useRef(new Audio.Sound()); // UNCOMMENT AFTER INSTALL

	useEffect(() => {
        let isMounted = true;
        /* UNCOMMENT THIS BLOCK AFTER INSTALLING expo-av
        */
	   const loadAndPlaySound = async () => {
            try {
                await Audio.setAudioModeAsync({ playsInSilentModeIOS: true }); // UNCOMMENT
                await soundObject.current.loadAsync(require('../../../assets/audio/attention.mp3')); // UNCOMMENT
                await soundObject.current.setIsLoopingAsync(true); // UNCOMMENT
                if(isMounted) {
                    await soundObject.current.playAsync(); // UNCOMMENT
                }
            } catch (error) {
                console.error("Failed to load and play sound for warning notification:", error);
            }
        };

		loadAndPlaySound();

		Animated.loop(
			Animated.sequence([
				Animated.timing(flashAnimation, { toValue: 1, duration: 500, useNativeDriver: false }),
				Animated.timing(flashAnimation, { toValue: 0, duration: 500, useNativeDriver: false }),
			])
		).start();

		return () => {
            isMounted = false;
			soundObject.current.unloadAsync(); // UNCOMMENT AFTER INSTALL
			flashAnimation.stopAnimation();
		};
	}, [flashAnimation]);

	if (!notification) return null;

	const backgroundColor = flashAnimation.interpolate({
		inputRange: [0, 1],
		outputRange: ['rgba(0,0,0,0.6)', `rgba(${colors.dangerRgb}, 0.6)`]
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
					<Icon name="exclamation-triangle" size={60} color={colors.danger} />
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

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        overlay: {
            flex: 1,
            justifyContent: 'center',
            alignItems: 'center',
            padding: 20,
        },
        modalContent: {
            width: '100%',
            maxWidth: 500,
            backgroundColor: colors.surface,
            borderRadius: 8,
            padding: 24,
            alignItems: 'center',
            borderWidth: 3,
            borderColor: colors.danger,
        },
        title: {
            fontSize: typography.h2,
            fontWeight: 'bold',
            color: colors.danger,
            marginTop: spacing.md,
            marginBottom: spacing.sm,
        },
        description: {
            fontSize: typography.h4,
            textAlign: 'center',
            marginBottom: spacing.lg,
            color: colors.text,
        },
        button: {
            backgroundColor: colors.danger,
            paddingVertical: 12,
            paddingHorizontal: 30,
            borderRadius: 6,
        },
        buttonText: {
            color: colors.white,
            fontSize: 16,
            fontWeight: 'bold',
        },
    });
};

export default WarningNotification;