import React from 'react';
import { TouchableOpacity, Text, StyleSheet } from 'react-native';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';

const UpdateNotification = ({ onUpdate }) => {
	const { addToast } = useToast();

	const handleUpdate = () => {
		addToast("Anwendung wird aktualisiert...", "info");
		// In React Native, this would likely trigger a library like react-native-code-push
		// For now, it just calls the passed function.
		setTimeout(onUpdate, 1000);
	};

	return (
		<TouchableOpacity style={styles.container} onPress={handleUpdate}>
			<Text style={styles.text}>Eine neue Version ist verfügbar. Klicken, um zu aktualisieren.</Text>
			<Icon name="sync-alt" size={16} color="#000" />
		</TouchableOpacity>
	);
};

const styles = StyleSheet.create({
	container: {
		position: 'absolute',
		bottom: 20,
		left: 20,
		right: 20,
		zIndex: 10000,
		backgroundColor: '#0dcaf0', // info-color
		padding: 16,
		borderRadius: 8,
		flexDirection: 'row',
		justifyContent: 'space-between',
		alignItems: 'center',
		shadowColor: "#000",
		shadowOffset: { width: 0, height: 2 },
		shadowOpacity: 0.25,
		shadowRadius: 3.84,
		elevation: 5,
	},
	text: {
		color: '#000',
		fontWeight: '500',
		flex: 1,
	},
});

export default UpdateNotification;