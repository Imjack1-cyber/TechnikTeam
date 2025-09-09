import React from 'react';
import { Modal as RNModal, View, Text, StyleSheet, TouchableOpacity, TouchableWithoutFeedback } from 'react-native';

const Modal = ({ isOpen, onClose, title, children }) => {
	if (!isOpen) {
		return null;
	}

	return (
		<RNModal
			transparent={true}
			visible={isOpen}
			onRequestClose={onClose}
			animationType="fade"
		>
			<TouchableWithoutFeedback onPress={onClose}>
				<View style={styles.modalOverlay}>
					<TouchableWithoutFeedback>
						<View style={styles.modalContent}>
							<TouchableOpacity
								style={styles.modalCloseBtn}
								onPress={onClose}
								hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
							>
								<Text style={styles.modalCloseText}>×</Text>
							</TouchableOpacity>
							{title && <Text style={styles.modalTitle}>{title}</Text>}
							{children}
						</View>
					</TouchableWithoutFeedback>
				</View>
			</TouchableWithoutFeedback>
		</RNModal>
	);
};

const styles = StyleSheet.create({
	modalOverlay: {
		flex: 1,
		backgroundColor: 'rgba(0, 0, 0, 0.6)',
		justifyContent: 'center',
		alignItems: 'center',
		padding: 20,
	},
	modalContent: {
		backgroundColor: '#ffffff', // This should be replaced with theme color
		padding: 24,
		borderRadius: 8,
		width: '100%',
		maxHeight: '90%',
        flexShrink: 1, // Allow the container to shrink if content is large
		shadowColor: "#000",
		shadowOffset: {
			width: 0,
			height: 5,
		},
		shadowOpacity: 0.34,
		shadowRadius: 6.27,
		elevation: 10,
	},
	modalCloseBtn: {
		position: 'absolute',
		top: 10,
		right: 10,
		zIndex: 1,
		padding: 8,
	},
	modalCloseText: {
		fontSize: 24,
		color: '#6c757d', // Muted text color
	},
	modalTitle: {
		fontSize: 20,
		fontWeight: '600',
		marginBottom: 16,
		color: '#002B5B', // Heading color
	},
});


export default Modal;