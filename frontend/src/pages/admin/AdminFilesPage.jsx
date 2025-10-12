import React, { useState, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, SectionList, TouchableOpacity, ActivityIndicator, Alert, Platform, TextInput } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import UploadFileModal from '../../components/admin/files/UploadFileModal';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import AdminModal from '../../components/ui/AdminModal';
import * as DocumentPicker from 'expo-document-picker';
import FileShareModal from '../../components/admin/files/FileShareModal';
import ReplaceFileModal from '../../components/admin/files/ReplaceFileModal';
import ConfirmationModal from '../../components/ui/ConfirmationModal';

const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    if (!bytes) return '';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const RenameFileModal = ({ isOpen, onClose, onSuccess, file }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const { addToast } = useToast();
    const [newName, setNewName] = useState(file?.filename || '');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async () => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.put(`/admin/files/${file.id}/rename`, { newName });
            if (result.success) {
                addToast('Datei umbenannt.', 'success');
                onSuccess();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <AdminModal isOpen={isOpen} onClose={onClose} title="Datei umbenennen" onSubmit={handleSubmit} isSubmitting={isSubmitting}>
            <Text style={styles.label}>Neuer Dateiname</Text>
            <TextInput style={styles.input} value={newName} onChangeText={setNewName} placeholderTextColor={colors.textMuted}/>
        </AdminModal>
    );
};

const RenameCategoryModal = ({ isOpen, onClose, onSuccess, category }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const { addToast } = useToast();
    const [newName, setNewName] = useState(category?.name || '');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async () => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.put(`/admin/files/categories/${category.id}`, { name: newName });
            if (result.success) {
                addToast('Kategorie umbenannt.', 'success');
                onSuccess();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };
    
    return (
        <AdminModal isOpen={isOpen} onClose={onClose} title="Kategorie umbenennen" onSubmit={handleSubmit} isSubmitting={isSubmitting}>
             <Text style={styles.label}>Neuer Kategoriename</Text>
            <TextInput style={styles.input} value={newName} onChangeText={setNewName} placeholderTextColor={colors.textMuted}/>
        </AdminModal>
    );
};

const AdminFilesPage = () => {
	const filesApiCall = useCallback(() => apiClient.get('/admin/files'), []);
	const { data: fileApiResponse, loading, error, reload: reloadFiles } = useApi(filesApiCall, { subscribeTo: ['FILE', 'FILE_CATEGORY'] });
    const { addToast } = useToast();
    const navigation = useNavigation();
    const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
    const [modalState, setModalState] = useState({ type: null, data: null });

    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const sections = useMemo(() => {
		if (!fileApiResponse?.grouped) return [];
		return Object.entries(fileApiResponse.grouped).map(([categoryName, files]) => ({
			title: categoryName,
            id: files[0]?.categoryId, // Grab categoryId from the first file
			data: files,
		}));
	}, [fileApiResponse]);

	const handleModalSuccess = () => {
        setModalState({ type: null, data: null });
        reloadFiles();
    };
    const closeModal = () => setModalState({ type: null, data: null });


	const handleDeleteFile = async () => {
        const file = modalState.data;
        closeModal();
        try {
            const result = await apiClient.delete(`/admin/files/${file.id}`);
            if (result.success) {
                addToast('Datei gelöscht', 'success');
                reloadFiles();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(err.message, 'error'); }
	};

    const handleDeleteCategory = async () => {
        const category = modalState.data;
        closeModal();
        try {
            const result = await apiClient.delete(`/admin/files/categories/${category.id}`);
            if (result.success) {
                addToast('Kategorie gelöscht.', 'success');
                reloadFiles();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(err.message, 'error'); }
    };

    const handleUploadSuccess = () => {
        setIsUploadModalOpen(false);
        reloadFiles();
    };

    const renderItem = ({ item }) => {
        const isMarkdown = item.filename.toLowerCase().endsWith('.md');
        return (
            <View style={styles.fileRow}>
                <View style={styles.fileInfo}>
                    <Icon name="file-alt" solid size={16} style={styles.fileIcon} />
                    <Text style={styles.fileName}>{item.filename}</Text>
                </View>
                <View style={styles.fileActions}>
                    <TouchableOpacity onPress={() => setModalState({ type: 'shareFile', data: item })}>
                        <Icon name="share-alt" size={18} color={colors.textMuted} />
                    </TouchableOpacity>
                    {isMarkdown && (
                        <TouchableOpacity onPress={() => navigation.navigate('AdminFileEditor', { fileId: item.id })}>
                            <Icon name="pen-alt" size={18} color={colors.textMuted} />
                        </TouchableOpacity>
                    )}
                    <TouchableOpacity onPress={() => setModalState({ type: 'renameFile', data: item })}>
                        <Icon name="i-cursor" size={18} color={colors.textMuted} />
                    </TouchableOpacity>
                    <TouchableOpacity onPress={() => setModalState({ type: 'replaceFile', data: item })}>
                        <Icon name="file-import" size={18} color={colors.textMuted} />
                    </TouchableOpacity>
                    <TouchableOpacity onPress={() => setModalState({ type: 'confirmDeleteFile', data: item })}>
                        <Icon name="trash" size={18} color={colors.danger} />
                    </TouchableOpacity>
                </View>
            </View>
        );
    };

    const renderSectionHeader = ({ section: { title, id } }) => (
        <View style={styles.sectionHeader}>
            <View style={{flexDirection: 'row', alignItems: 'center', flex: 1}}>
                <Icon name="folder" solid size={18} style={styles.folderIcon} />
                <Text style={styles.sectionTitle}>{title}</Text>
            </View>
            {id && ( // Don't show for "Ohne Kategorie"
                <View style={{flexDirection: 'row', gap: 24}}>
                    <TouchableOpacity onPress={() => setModalState({ type: 'renameCategory', data: { id, name: title } })}>
                        <Icon name="edit" size={18} color={colors.textMuted} />
                    </TouchableOpacity>
                    <TouchableOpacity onPress={() => setModalState({ type: 'confirmDeleteCategory', data: { id, name: title } })}>
                        <Icon name="trash" size={18} color={colors.danger} />
                    </TouchableOpacity>
                </View>
            )}
        </View>
    );

	return (
		<View style={styles.container}>
			<View style={styles.headerContainer}>
                <Icon name="file-upload" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Datei-Verwaltung</Text>
			</View>
            <Text style={styles.subtitle}>Hier können Sie alle zentralen Dokumente und Vorlagen verwalten.</Text>
            
            <TouchableOpacity style={[styles.button, styles.successButton, { marginHorizontal: 16, marginBottom: 16}]} onPress={() => setIsUploadModalOpen(true)}>
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
                ListEmptyComponent={<View style={styles.card}><Text style={{color: colors.text}}>Keine Dateien gefunden.</Text></View>}
            />
            
            <UploadFileModal
                isOpen={isUploadModalOpen}
                onClose={() => setIsUploadModalOpen(false)}
                onSuccess={handleUploadSuccess}
                formatFileSize={formatFileSize}
            />
            {modalState.type === 'renameFile' && <RenameFileModal isOpen={true} onClose={closeModal} onSuccess={handleModalSuccess} file={modalState.data} />}
            {modalState.type === 'replaceFile' && <ReplaceFileModal isOpen={true} onClose={closeModal} onSuccess={handleModalSuccess} file={modalState.data} formatFileSize={formatFileSize} />}
            {modalState.type === 'renameCategory' && <RenameCategoryModal isOpen={true} onClose={closeModal} onSuccess={handleModalSuccess} category={modalState.data} />}
            {modalState.type === 'confirmDeleteFile' && <ConfirmationModal isOpen={true} onClose={closeModal} onConfirm={handleDeleteFile} title="Datei löschen" message={`Datei "${modalState.data.filename}" wirklich löschen?`} />}
            {modalState.type === 'confirmDeleteCategory' && <ConfirmationModal isOpen={true} onClose={closeModal} onConfirm={handleDeleteCategory} title="Kategorie löschen" message={`Kategorie "${modalState.data.name}" wirklich löschen?`} />}
            {modalState.type === 'shareFile' && <FileShareModal isOpen={true} onClose={closeModal} file={modalState.data} />}
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
        fileActions: { flexDirection: 'row', gap: 24, alignItems: 'center' },
    });
};

export default AdminFilesPage;