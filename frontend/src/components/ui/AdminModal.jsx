import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
} from 'react-native';
import Modal from './Modal';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing } from '../../styles/theme';

const AdminModal = ({
  isOpen,
  onClose,
  title,
  children,
  onSubmit,
  isSubmitting,
  submitText = 'Speichern',
  submitButtonVariant = 'primary',
}) => {
  const theme = useAuthStore((state) => state.theme);
  const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
  const colors = getThemeColors(theme);

  const getSubmitButtonVariant = () => {
    switch (submitButtonVariant) {
      case 'success':
        return styles.successButton;
      case 'danger':
        return styles.dangerButton;
      case 'primary':
      default:
        return styles.primaryButton;
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title}>
      <View style={styles.modalContainer}>
        <ScrollView
          style={styles.modalBody}
          contentContainerStyle={styles.modalBodyContent}
          keyboardShouldPersistTaps="handled"
        >
          {children}
        </ScrollView>
        {onSubmit && (
          <View style={styles.modalFooter}>
            <TouchableOpacity
              style={[styles.button, styles.secondaryButton]}
              onPress={onClose}
              disabled={isSubmitting}
            >
              <Text style={styles.buttonText}>Abbrechen</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[
                styles.button,
                getSubmitButtonVariant(),
                isSubmitting && styles.disabledButton,
              ]}
              onPress={onSubmit}
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <ActivityIndicator color={colors.white} />
              ) : (
                <Text style={styles.buttonText}>{submitText}</Text>
              )}
            </TouchableOpacity>
          </View>
        )}
      </View>
    </Modal>
  );
};

const pageStyles = (theme) => {
  const colors = getThemeColors(theme);
  return StyleSheet.create({
    modalContainer: {
      flexShrink: 1,
      flexGrow: 1,
      minHeight: 100,
    },
    modalBody: {
      flexGrow: 1,
    },
    modalBodyContent: {
      flexGrow: 1,
      paddingBottom: spacing.lg,
    },
    modalFooter: {
      flexDirection: 'row',
      justifyContent: 'flex-end',
      gap: spacing.sm,
      paddingTop: spacing.md,
      borderTopWidth: 1,
      borderColor: colors.border,
    },
  });
};

export default AdminModal;
