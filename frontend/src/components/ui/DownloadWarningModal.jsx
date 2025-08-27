import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import Modal from './Modal';
import Icon from 'react-native-vector-icons/FontAwesome5';

const DownloadWarningModal = ({ isOpen, onClose, onConfirm, file }) => {
	if (!isOpen || !file) return null;

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Download-Warnung">
			<View style={styles.container}>
				<Icon name="exclamation-triangle" size={48} color="#ffc107" style={styles.icon} />
				<Text style={styles.title}>Potenziell unsichere Datei</Text>
				<Text style={styles.description}>
					Sie sind im Begriff, die Datei <Text style={{ fontWeight: 'bold' }}>"{file.filename}"</Text> herunterzuladen.
				</Text>
				<Text style={styles.description}>
					Dateien dieses Typs könnten potenziell schädlichen Code enthalten. Öffnen Sie diese Datei nur, wenn Sie der Quelle vertrauen.
				</Text>
				<Text style={styles.question}>
					Möchten Sie den Download fortsetzen?
				</Text>
				<View style={styles.buttonContainer}>
					<TouchableOpacity onPress={onClose} style={[styles.button, styles.secondaryButton]}>
						<Text style={styles.secondaryButtonText}>Abbrechen</Text>
					</TouchableOpacity>
					<TouchableOpacity onPress={onConfirm} style={[styles.button, styles.dangerButton]}>
						<Text style={styles.buttonText}>Ja, herunterladen</Text>
					</TouchableOpacity>
				</View>
			</View>
		</Modal>
	);
};

const styles = StyleSheet.create({
	container: {
		alignItems: 'center',
	},
	icon: {
		marginBottom: 16,
	},
	title: {
		fontSize: 20,
		fontWeight: 'bold',
		marginBottom: 8,
	},
	description: {
		textAlign: 'center',
		fontSize: 16,
		marginBottom: 8,
		color: '#212529',
	},
	question: {
		fontWeight: 'bold',
		textAlign: 'center',
		fontSize: 16,
		marginTop: 16,
	},
	buttonContainer: {
		flexDirection: 'row',
		justifyContent: 'center',
		gap: 16,
		marginTop: 24,
	},
	button: {
		paddingVertical: 10,
		paddingHorizontal: 20,
		borderRadius: 6,
	},
	secondaryButton: {
		backgroundColor: '#6c757d',
	},
	dangerButton: {
		backgroundColor: '#dc3545',
	},
	buttonText: {
		color: '#fff',
		fontWeight: '500',
	},
	secondaryButtonText: {
		color: '#fff',
		fontWeight: '500',
	},
});

export default DownloadWarningModal;