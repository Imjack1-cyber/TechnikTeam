import React from 'react';
import { Modal as RNModal, View, Text, StyleSheet, TouchableOpacity, TouchableWithoutFeedback } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing, borders, shadows } from '../../styles/theme';

const Modal = ({ isOpen, onClose, title, children }) => {
	if (!isOpen) {
		return null;
	}

    const theme = useAuthStore.getState().theme;
    const modalStyles = styles(theme);

	return (
		<RNModal
			transparent={true}
			visible={isOpen}
			onRequestClose={onClose}
			animationType="fade"
		>
			<TouchableWithoutFeedback onPress={onClose}>
				<View style={modalStyles.modalOverlay}>
					<TouchableWithoutFeedback>
						<View style={modalStyles.modalContent}>
							<TouchableOpacity
								style={modalStyles.modalCloseBtn}
								onPress={onClose}
								hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
							>
								<Text style={modalStyles.modalCloseText}>×</Text>
							</TouchableOpacity>
							{title && <Text style={modalStyles.modalTitle}>{title}</Text>}
							{children}
						</View>
					</TouchableWithoutFeedback>
				</View>
			</TouchableWithoutFeedback>
		</RNModal>
	);
};

const styles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        modalOverlay: {
            flex: 1,
            backgroundColor: 'rgba(0, 0, 0, 0.6)',
            justifyContent: 'center',
            alignItems: 'center',
            padding: 20,
        },
        modalContent: {
            backgroundColor: colors.surface,
            padding: spacing.lg,
            borderRadius: borders.radius,
            width: '100%',
            maxHeight: '90%',
            ...shadows.lg,
            // Use flexbox to allow content (like ScrollViews in AdminModal) to expand correctly.
            flexDirection: 'column',
            // Allow the modal to shrink to its content's size if the content is small.
            flexShrink: 1,
        },
        modalCloseBtn: {
            position: 'absolute',
            top: spacing.md,
            right: spacing.md,
            zIndex: 1,
            padding: spacing.sm,
        },
        modalCloseText: {
            fontSize: 24,
            color: colors.textMuted,
        },
        modalTitle: {
            fontSize: typography.h3,
            fontWeight: '600',
            marginBottom: spacing.md,
            color: colors.heading,
        },
    });
};


export default Modal;