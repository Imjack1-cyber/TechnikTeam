import React from 'react';
import { Modal, View, Image, StyleSheet, TouchableOpacity, Text, SafeAreaView } from 'react-native';

const Lightbox = ({ src, onClose }) => {
	if (!src) {
		return null;
	}

	return (
		<Modal
			visible={!!src}
			transparent={true}
			animationType="fade"
			onRequestClose={onClose}
		>
			<SafeAreaView style={styles.lightboxOverlay}>
				<TouchableOpacity style={styles.closeButton} onPress={onClose}>
					<Text style={styles.closeText}>×</Text>
				</TouchableOpacity>
				<Image
					source={{ uri: src }}
					style={styles.lightboxContent}
					resizeMode="contain"
				/>
			</SafeAreaView>
		</Modal>
	);
};

const styles = StyleSheet.create({
	lightboxOverlay: {
		flex: 1,
		backgroundColor: 'rgba(0, 0, 0, 0.85)',
		justifyContent: 'center',
		alignItems: 'center',
	},
	lightboxContent: {
		width: '100%',
		height: '100%',
	},
	closeButton: {
		position: 'absolute',
		top: 40,
		right: 20,
		zIndex: 1,
		padding: 10,
	},
	closeText: {
		color: '#fff',
		fontSize: 30,
		fontWeight: 'bold',
	},
});

export default Lightbox;