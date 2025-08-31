import React, { useState, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, SectionList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
// NOTE: File picking requires a native library.
// import DocumentPicker from 'react-native-document-picker';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../../components/ui/Modal';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const AdminFilesPage = ({ navigation }) => {
	const filesApiCall = useCallback(() => apiClient.get('/admin/files'), []);
	const { data: fileApiResponse, loading, error, reload: reloadFiles } = useApi(filesApiCall);
    const { addToast } = useToast();

    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const sections = useMemo(() => {
		if (!fileApiResponse?.grouped) return [];
		return Object.entries(fileApiResponse.grouped).map(([categoryName, files]) => ({
			title: categoryName,
			data: files,
		}));
	}, [fileApiResponse]);

	const handleDeleteFile = (file) => {
		Alert.alert(`Datei "${file.filename}" löschen?`, "Diese Aktion kann nicht rückgängig gemacht werden.", [
			{ text: 'Abbrechen', style: 'cancel' },
			{ text: 'Löschen', style: 'destructive', onPress: async () => {
				try {
					const result = await apiClient.delete(`/admin/files/${file.id}`);
					if (result.success) {
						addToast('Datei gelöscht', 'success');
						reloadFiles();
					} else { throw new Error(result.message); }
				} catch (err) { addToast(err.message, 'error'); }
			}},
		]);
	};

    const renderItem = ({ item }) => {
        const isMarkdown = item.filename.toLowerCase().endsWith('.md');
        return (
            <View style={styles.fileRow}>
                <View style={styles.fileInfo}>
                    <Icon name="download" size={16} color={styles.fileIcon.color} />
                    <Text style={styles.fileName}>{item.filename}</Text>
                    <Text style={styles.fileMeta}>Sichtbarkeit: {item.requiredRole}</Text>
                </View>
                <View style={styles.fileActions}>
                    {isMarkdown && (
                        <TouchableOpacity onPress={() => navigation.navigate('AdminFileEditor', { fileId: item.id })}>
                            <Icon name="pen-alt" size={18} color={colors.textMuted} />
                        </TouchableOpacity>
                    )}
                    <TouchableOpacity onPress={() => handleDeleteFile(item)}>
                        <Icon name="trash" size={18} color={colors.danger} />
                    </TouchableOpacity>
                </View>
            </View>
        );
    };

    const renderSectionHeader = ({ section: { title } }) => (
        <View style={styles.sectionHeader}>
            <Icon name="folder" solid size={18} style={styles.folderIcon} />
            <Text style={styles.sectionTitle}>{title}</Text>
        </View>
    );

	return (
		<View style={styles.container}>
			<View style={styles.headerContainer}>
                <Icon name="file-upload" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Datei-Verwaltung</Text>
			</View>
            <Text style={styles.subtitle}>Hier können Sie alle zentralen Dokumente und Vorlagen verwalten.</Text>
            
            {/* NOTE: File upload requires a native module and would open a modal */}
            <TouchableOpacity style={[styles.button, styles.successButton, { marginHorizontal: 16, marginBottom: 16}]}>
                <Icon name="upload" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neue Datei hochladen</Text>
            </TouchableOpacity>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<SectionList
                sections={sections}
                keyExtractor={(item) => item.id.toString()}
                renderItem={renderItem}
                renderSectionHeader={renderSectionHeader}
                ListEmptyComponent={<View style={styles.card}><Text>Keine Dateien gefunden.</Text></View>}
            />
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center', padding: 16 },
        headerIcon: { color: colors.heading, marginRight: 12 },
        sectionHeader: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.background, paddingVertical: 8, paddingHorizontal: 16, borderBottomWidth: 1, borderTopWidth: 1, borderColor: colors.border },
        folderIcon: { marginRight: 8, color: colors.textMuted },
        sectionTitle: { fontSize: typography.h4, fontWeight: 'bold', color: colors.heading },
        fileRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', padding: 16, backgroundColor: colors.surface, borderBottomWidth: 1, borderColor: colors.border },
        fileInfo: { flex: 1, flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
        fileIcon: { color: colors.primary },
        fileName: { fontSize: typography.body, color: colors.text, flexShrink: 1 },
        fileMeta: { fontSize: typography.caption, color: colors.textMuted, marginLeft: 8, marginTop: 4 },
        fileActions: { flexDirection: 'row', gap: 24, alignItems: 'center' },
    });
};

export default AdminFilesPage;