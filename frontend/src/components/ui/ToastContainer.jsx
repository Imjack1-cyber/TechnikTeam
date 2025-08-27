import React, { useEffect, useRef } from 'react';
import { View, Text, StyleSheet, Animated, TouchableOpacity } from 'react-native';
import { useToast } from '../../context/ToastContext';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNavigation } from '@react-navigation/native';
import Icon from 'react-native-vector-icons/FontAwesome5';

const Toast = ({ message, type, url, onHide }) => {
	const fadeAnim = useRef(new Animated.Value(0)).current;
	const navigation = useNavigation();

	useEffect(() => {
		Animated.timing(fadeAnim, {
			toValue: 1,
			duration: 300,
			useNativeDriver: true,
		}).start();

		const timer = setTimeout(() => {
			Animated.timing(fadeAnim, {
				toValue: 0,
				duration: 300,
				useNativeDriver: true,
			}).start(() => {
				onHide();
			});
		}, 4600);
		return () => clearTimeout(timer);
	}, [fadeAnim, onHide]);


	const getToastStyle = () => {
		switch (type) {
			case 'success':
				return styles.success;
			case 'error':
				return styles.danger;
			default:
				return styles.info;
		}
	};

	const handlePress = () => {
		if (url) {
			// Basic URL parsing to navigate
			// This needs to be more robust in a real app
			const route = url.startsWith('/') ? url.substring(1) : url;
			navigation.navigate(route);
		}
	};

	const toastContent = (
		<Animated.View style={[styles.toast, getToastStyle(), { opacity: fadeAnim }]}>
			<Text style={styles.toastText}>{message}</Text>
			{url && <Icon name="arrow-right" size={14} color="#fff" style={styles.icon} />}
		</Animated.View>
	);

	if (url) {
		return <TouchableOpacity onPress={handlePress}>{toastContent}</TouchableOpacity>
	}

	return toastContent;
};


const ToastContainer = () => {
	const { toasts } = useToast();
	const insets = useSafeAreaInsets();

	return (
		<View style={[styles.container, { bottom: insets.bottom + 20, right: 20, left: 20 }]}>
			{toasts.map(toast => (
				<Toast key={toast.id} message={toast.message} type={toast.type} url={toast.url} onHide={() => { }} />
			))}
		</View>
	);
};

const styles = StyleSheet.create({
	container: {
		position: 'absolute',
		zIndex: 9999,
	},
	toast: {
		paddingVertical: 12,
		paddingHorizontal: 18,
		borderRadius: 8,
		shadowColor: "#000",
		shadowOffset: {
			width: 0,
			height: 2,
		},
		shadowOpacity: 0.25,
		shadowRadius: 3.84,
		elevation: 5,
		marginBottom: 8,
		flexDirection: 'row',
		alignItems: 'center',
		justifyContent: 'space-between',
	},
	success: {
		backgroundColor: '#28a745',
	},
	danger: {
		backgroundColor: '#dc3545',
	},
	info: {
		backgroundColor: '#0dcaf0',
	},
	toastText: {
		color: '#fff',
		fontWeight: '500',
        flex: 1,
	},
    icon: {
        marginLeft: 16,
    }
});

export default ToastContainer;