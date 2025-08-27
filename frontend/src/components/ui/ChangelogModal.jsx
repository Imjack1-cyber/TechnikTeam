import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import Modal from './Modal';
import MarkdownDisplay from 'react-native-markdown-display';

const ChangelogModal = ({ changelog, onClose }) => {
	return (
		<Modal isOpen={true} onClose={onClose} title={`Was ist neu in Version ${changelog.version}?`}>
			<View>
				<Text style={styles.title}>{changelog.title}</Text>
				<Text style={styles.subtitle}>
					Veröffentlicht am {new Date(changelog.releaseDate).toLocaleDateString('de-DE')}
				</Text>
				<ScrollView style={styles.markdownContainer}>
					<MarkdownDisplay>
						{changelog.notes}
					</MarkdownDisplay>
				</ScrollView>
				<View style={styles.buttonContainer}>
					<TouchableOpacity onPress={onClose} style={styles.button}>
						<Text style={styles.buttonText}>Verstanden!</Text>
					</TouchableOpacity>
				</View>
			</View>
		</Modal>
	);
};

const styles = StyleSheet.create({
	title: {
		fontSize: 18,
		fontWeight: 'bold',
		color: '#002B5B', // heading-color
		marginBottom: 4,
	},
	subtitle: {
		fontSize: 14,
		color: '#6c757d', // text-muted-color
		marginBottom: 16,
	},
	markdownContainer: {
		maxHeight: '60%',
		borderWidth: 1,
		borderColor: '#dee2e6', // border-color
		paddingHorizontal: 8,
		borderRadius: 8,
	},
	buttonContainer: {
		flexDirection: 'row',
		justifyContent: 'flex-end',
		marginTop: 24,
	},
	button: {
		backgroundColor: '#007bff', // primary-color
		paddingVertical: 10,
		paddingHorizontal: 20,
		borderRadius: 6,
	},
	buttonText: {
		color: '#ffffff',
		fontWeight: '500',
	}
});

export default ChangelogModal;