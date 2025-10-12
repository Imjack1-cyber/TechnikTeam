import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import Modal from './Modal';
import MarkdownDisplay from 'react-native-markdown-display';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing } from '../../styles/theme';

const ChangelogModal = ({ changelog, onClose }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	if (!changelog) return null;

	return (
		<Modal isOpen={true} onClose={onClose} title={`Was ist neu in Version ${changelog.version}?`}>
			<View>
				<Text style={styles.title}>{changelog.title}</Text>
				<Text style={styles.subtitle}>
					Veröffentlicht am {new Date(changelog.releaseDate).toLocaleDateString('de-DE')}
				</Text>
				<ScrollView style={styles.modalMarkdownContainer}>
					<MarkdownDisplay style={{ body: { padding: 12, color: colors.text } }}>
						{changelog.notes}
					</MarkdownDisplay>
				</ScrollView>
				<View style={styles.buttonContainer}>
					<TouchableOpacity onPress={onClose} style={[styles.button, styles.primaryButton]}>
						<Text style={styles.buttonText}>Verstanden!</Text>
					</TouchableOpacity>
				</View>
			</View>
		</Modal>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        modalMarkdownContainer: {
            maxHeight: '80%',
            borderWidth: 1,
            borderColor: colors.border,
            borderRadius: 6,
            marginTop: 12,
        },
        buttonContainer: {
            flexDirection: 'row',
            justifyContent: 'flex-end',
            marginTop: 24,
        },
    });
};

export default ChangelogModal;