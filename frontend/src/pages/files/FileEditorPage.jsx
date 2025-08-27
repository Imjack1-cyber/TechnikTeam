import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, TextInput, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import MarkdownDisplay from 'react-native-markdown-display';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';

const FileEditorPage = () => {
	const route = useRoute();
    const navigation = useNavigation();
	const { fileId } = route.params;
	const { addToast } = useToast();
	const { isAdmin } = useAuthStore();
	const [content, setContent] = useState('');
	const [initialContent, setInitialContent] = useState(null);
	const [viewMode, setViewMode] = useState('edit');
	const [saveStatus, setSaveStatus] = useState('idle');
	const [error, setError] = useState('');

	const theme = useAuthStore(state => state.theme);
	const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const apiCall = useCallback(() => {
		const endpoint = isAdmin ? `/admin/files/content/${fileId}` : `/public/files/content/${fileId}`;
		return apiClient.get(endpoint);
	}, [fileId, isAdmin]);

	const { data: fileData, loading, error: fetchError } = useApi(apiCall);

	useEffect(() => {
		if (fileData?.content !== null && fileData?.content !== undefined) {
			setContent(fileData.content);
			setInitialContent(fileData.content);
		}
	}, [fileData]);

	const handleSave = useCallback(async () => {
		if (saveStatus === 'saving' || content === initialContent) return;

		setSaveStatus('saving');
		setError('');
		try {
			const endpoint = isAdmin ? `/admin/files/content/${fileId}` : `/public/files/content/${fileId}`;
			const result = await apiClient.put(endpoint, { content });
			if (result.success) {
				setInitialContent(content);
				setSaveStatus('saved');
				setTimeout(() => setSaveStatus('idle'), 2000);
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Speichern fehlgeschlagen.');
			setSaveStatus('idle');
			addToast(`Fehler beim Speichern: ${err.message}`, 'error');
		}
	}, [fileId, isAdmin, content, initialContent, saveStatus, addToast]);

    useEffect(() => {
		const interval = setInterval(handleSave, 5000);
		return () => clearInterval(interval);
	}, [handleSave]);

	const getSaveButtonContent = () => {
		switch (saveStatus) {
			case 'saving': return <ActivityIndicator color="#fff" />;
			case 'saved': return <Text style={styles.buttonText}>Gespeichert!</Text>;
			default: return <Text style={styles.buttonText}>Jetzt Speichern</Text>;
		}
	};

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (fetchError) return <View style={styles.centered}><Text style={styles.errorText}>{fetchError}</Text></View>;

	return (
		<View style={styles.container}>
            <View style={styles.header}>
                <Text style={styles.title}>Editor: {fileData?.filename}</Text>
            </View>
            <View style={styles.controls}>
                <View style={styles.tabs}>
                    <TouchableOpacity style={[styles.tabButton, viewMode === 'edit' && styles.activeTab]} onPress={() => setViewMode('edit')}>
                        <Text style={[styles.tabText, viewMode === 'edit' && {color: colors.white}]}>Bearbeiten</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={[styles.tabButton, viewMode === 'preview' && styles.activeTab]} onPress={() => setViewMode('preview')}>
                        <Text style={[styles.tabText, viewMode === 'preview' && {color: colors.white}]}>Vorschau</Text>
                    </TouchableOpacity>
                </View>
                <TouchableOpacity style={[styles.button, saveStatus === 'saved' ? styles.successButton : styles.primaryButton]} onPress={handleSave} disabled={saveStatus === 'saving' || content === initialContent}>
                    {getSaveButtonContent()}
                </TouchableOpacity>
            </View>
            {error && <Text style={styles.errorText}>{error}</Text>}

			<View style={styles.editorContainer}>
				{viewMode === 'edit' ? (
					<TextInput value={content} onChangeText={setContent} style={styles.textArea} multiline textAlignVertical="top" />
				) : (
					<ScrollView><View style={{padding: 10}}><MarkdownDisplay>{content}</MarkdownDisplay></View></ScrollView>
				)}
			</View>
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: { padding: 16 },
        controls: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 16, marginBottom: 16 },
        tabs: { flexDirection: 'row', borderWidth: 1, borderColor: colors.border, borderRadius: 8, overflow: 'hidden' },
        tabButton: { paddingVertical: 8, paddingHorizontal: 16 },
        activeTab: { backgroundColor: colors.primary },
        tabText: { color: colors.text },
        editorContainer: { flex: 1, borderWidth: 1, borderColor: colors.border, margin: 16, borderRadius: 8, backgroundColor: colors.surface },
        textArea: { flex: 1, padding: 10, fontSize: 14, fontFamily: 'monospace', color: colors.text },
    });
};

export default FileEditorPage;