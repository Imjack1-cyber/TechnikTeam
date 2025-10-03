import React, { useState, useEffect, useCallback, useRef } from 'react';
import { View, Text, TextInput, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, useWindowDimensions } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import useWebSocket from '../../hooks/useWebSocket';
import { debounce } from 'lodash';
import ConfirmationModal from '../../components/ui/ConfirmationModal';

const WikiEditorPage = () => {
    const route = useRoute();
    const navigation = useNavigation();
	const { entryId, entryPath } = route.params;
	const { addToast } = useToast();
	const [content, setContent] = useState('');
	const [viewMode, setViewMode] = useState('edit');
    const [deletingEntry, setDeletingEntry] = useState(null);
    const [isSubmittingDelete, setIsSubmittingDelete] = useState(false);
    const isTypingRef = useRef(false);

	const theme = useAuthStore(state => state.theme);
	const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const apiCall = useCallback(() => apiClient.get(`/wiki/${entryId}`), [entryId]);
	const { data: fileData, loading, error: fetchError, reload } = useApi(apiCall);

	useEffect(() => {
        navigation.setOptions({ title: `Wiki: ${entryPath}` });
		if (fileData?.content !== null && fileData?.content !== undefined) {
            if (!isTypingRef.current) {
			    setContent(fileData.content);
            }
		}
	}, [fileData, navigation, entryPath]);

    const handleWebSocketMessage = useCallback((message) => {
        if (message.type === 'content_update') {
            if (!isTypingRef.current) {
                setContent(message.payload.content);
            }
        }
    }, []);

    const { sendMessage } = useWebSocket(`/ws/editor/${entryId}`, handleWebSocketMessage);
    
    const debouncedSend = useCallback(debounce((newContent) => {
        sendMessage({ type: 'content_update', payload: { content: newContent } });
        isTypingRef.current = false;
    }, 500), [sendMessage]);

    const handleContentChange = (text) => {
        setContent(text);
        isTypingRef.current = true;
        debouncedSend(text);
    };

    const handleSave = async () => {
		try {
			const result = await apiClient.put(`/wiki/${entryId}`, { content: content });
			if (result.success) {
				addToast('Seite gespeichert', 'success');
                reload();
			} else { throw new Error(result.message); }
		} catch (err) { addToast(`Fehler beim Speichern: ${err.message}`, 'error'); }
	};
    
	const confirmDelete = async () => {
        if (!deletingEntry) return;
        setIsSubmittingDelete(true);
        try {
            const result = await apiClient.delete(`/wiki/${deletingEntry.id}`);
            if (result.success) {
                addToast('Seite gelöscht', 'success');
                navigation.goBack();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
        finally {
            setIsSubmittingDelete(false);
            setDeletingEntry(null);
        }
	};

    useEffect(() => {
        navigation.setOptions({
            headerRight: () => (
                <TouchableOpacity onPress={() => setDeletingEntry(fileData)} style={{ marginRight: 16 }}>
                    <Icon name="trash" size={20} color={colors.danger} />
                </TouchableOpacity>
            )
        });
    }, [navigation, fileData, colors]);


    if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (fetchError) return <View style={styles.centered}><Text style={styles.errorText}>{fetchError}</Text></View>;
    if (!fileData) return <View style={styles.centered}><Text>Eintrag nicht gefunden.</Text></View>;

	return (
		<View style={styles.container}>
            <View style={styles.controls}>
                <View style={styles.tabs}>
                    <TouchableOpacity style={[styles.tabButton, viewMode === 'edit' && styles.activeTab]} onPress={() => setViewMode('edit')}>
                        <Text style={[styles.tabText, viewMode === 'edit' && styles.activeTabText]}>Bearbeiten</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={[styles.tabButton, viewMode === 'preview' && styles.activeTab]} onPress={() => setViewMode('preview')}>
                        <Text style={[styles.tabText, viewMode === 'preview' && styles.activeTabText]}>Vorschau</Text>
                    </TouchableOpacity>
                </View>
                <TouchableOpacity onPress={handleSave} style={[styles.button, styles.successButton]}>
                    <Icon name="save" size={16} color={colors.white} />
                    <Text style={styles.buttonText}> Speichern</Text>
                </TouchableOpacity>
            </View>
			
            {viewMode === 'edit' ? (
                <TextInput value={content} onChangeText={handleContentChange} multiline style={styles.textArea} />
            ) : (
                <ScrollView style={{padding: spacing.md}}><MarkdownDisplay>{content}</MarkdownDisplay></ScrollView>
            )}
            {deletingEntry && (
                <ConfirmationModal
                    isOpen={!!deletingEntry}
                    onClose={() => setDeletingEntry(null)}
                    onConfirm={confirmDelete}
                    title={`Seite "${deletingEntry.filePath}" löschen?`}
                    message="Diese Aktion kann nicht rückgängig gemacht werden."
                    confirmText="Löschen"
                    isSubmitting={isSubmittingDelete}
                />
            )}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        controls: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 16, borderBottomWidth: 1, borderBottomColor: colors.border },
        tabs: { flexDirection: 'row', borderWidth: 1, borderColor: colors.border, borderRadius: 8, overflow: 'hidden' },
        tabButton: { paddingVertical: 8, paddingHorizontal: 16 },
        activeTab: { backgroundColor: colors.primary },
        tabText: { color: colors.text },
        activeTabText: { color: colors.white },
        textArea: { flex: 1, padding: spacing.md, fontSize: 14, fontFamily: 'monospace', color: colors.text, textAlignVertical: 'top', backgroundColor: colors.surface },
    });
};

export default WikiEditorPage;