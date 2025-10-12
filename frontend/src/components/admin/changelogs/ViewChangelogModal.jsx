import React from 'react';
import { ScrollView, Text, TouchableOpacity } from 'react-native';
import Modal from '../../ui/Modal';
import MarkdownDisplay from 'react-native-markdown-display';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors } from '../../../styles/theme';

const ViewChangelogModal = ({ changelog, onClose }) => {
    if (!changelog) return null;
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);

    return (
        <Modal isOpen={true} onClose={onClose} title={`Version ${changelog.version} - ${changelog.title}`}>
            <Text style={styles.subtitle}>
                Veröffentlicht am {new Date(changelog.releaseDate).toLocaleDateString('de-DE')}
            </Text>
            <ScrollView style={{ maxHeight: '80%', borderWidth: 1, borderColor: colors.border, borderRadius: 6, marginTop: 12 }}>
                <MarkdownDisplay style={{ body: { padding: 12, color: colors.text } }}>
                    {changelog.notes}
                </MarkdownDisplay>
            </ScrollView>
            <TouchableOpacity style={[styles.button, styles.secondaryButton, { marginTop: 16 }]} onPress={onClose}>
                <Text style={styles.buttonText}>Schließen</Text>
            </TouchableOpacity>
        </Modal>
    );
};

export default ViewChangelogModal;