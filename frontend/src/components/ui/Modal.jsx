import React from 'react';
import {
  Modal as RNModal,
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TouchableWithoutFeedback,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing, borders, shadows } from '../../styles/theme';

const Modal = ({ isOpen, onClose, title, children }) => {
  if (!isOpen) return null;

  const theme = useAuthStore.getState().theme;
  const modalStyles = styles(theme);

  return (
    <RNModal
      transparent
      visible={isOpen}
      onRequestClose={onClose}
      animationType="fade"
    >
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={modalStyles.modalOverlay}>
          <TouchableWithoutFeedback onPress={() => {}}>
            <KeyboardAvoidingView
              style={modalStyles.modalContent}
              behavior={Platform.OS === 'ios' ? 'padding' : undefined}
            >
              <TouchableOpacity
                style={modalStyles.modalCloseBtn}
                onPress={onClose}
                hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
              >
                <Text style={modalStyles.modalCloseText}>×</Text>
              </TouchableOpacity>
              {title && <Text style={modalStyles.modalTitle}>{title}</Text>}
              <View style={modalStyles.modalChildren}>{children}</View>
            </KeyboardAvoidingView>
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
      width: '90%', // better than 100% for Android
      maxHeight: '90%',
      ...shadows.lg,
    },
    modalChildren: {
      flexShrink: 1,
      flexGrow: 1,
    },
    modalCloseBtn: {
      position: 'absolute',
      top: spacing.md,
      right: spacing.md,
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
