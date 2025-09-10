import React, { useState, useEffect, useCallback, useRef } from 'react';
import { View, Text, TextInput, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import MarkdownDisplay from 'react-native-markdown-display';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography } from '../../styles/theme';
import useWebSocket from '../../hooks/useWebSocket';
import { debounce } from 'lodash';

const FileEditorPage = () => {
	const route = useRoute();
    const navigation = useNavigation();
	const { fileId } = route.params;
	const { addToast } = useToast();
	const { isAdmin } = useAuthStore();
	const [content, setContent] = useState('');
	const [viewMode, setViewMode] = useState('edit');
	const [error, setError] = useState('');
    const isTypingRef = useRef(false);

	const theme = useAuthStore(state => state.theme);
	const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const apiCall = useCallback(() => {
		const endpoint = isAdmin ? `/admin/files/content/${fileId}` : `/public/files/content/${fileId}`;
		return apiClient.get(endpoint);
	}, [fileId, isAdmin]);

	const { data: fileData, loading, error: fetchError } = useApi(apiCall);

	useEffect(() => {
        if (fileData?.filename) {
            navigation.setOptions({ title: `Editor: ${fileData.filename}` });
        }
		if (fileData?.content !== null && fileData?.content !== undefined) {
            if (!isTypingRef.current) {
			    setContent(fileData.content);
            }
		}
	}, [fileData, navigation]);

    const handleWebSocketMessage = useCallback((message) => {
        if (message.type === 'content_update') {
            // Only update if the user is not currently typing, to avoid losing their changes.
            if (!isTypingRef.current) {
                setContent(message.payload.content);
            }
        }
    }, []);

    const { sendMessage } = useWebSocket(`/ws/editor/${fileId}`, handleWebSocketMessage);
    
    // Use lodash debounce to send updates only after the user stops typing for 500ms
    const debouncedSend = useCallback(debounce((newContent) => {
        sendMessage({ type: 'content_update', payload: { content: newContent } });
        isTypingRef.current = false;
    }, 500), [sendMessage]);

    const handleContentChange = (text) => {
        setContent(text);
        isTypingRef.current = true;
        debouncedSend(text);
    };

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (fetchError) return <View style={styles.centered}><Text style={styles.errorText}>{fetchError}</Text></View>;

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
            </View>
            {error && <Text style={styles.errorText}>{error}</Text>}

			<View style={styles.editorContainer}>
				{viewMode === 'edit' ? (
					<TextInput
						value={content}
						onChangeText={handleContentChange}
						style={styles.textArea}
                        multiline
                        textAlignVertical="top"
					/>
				) : (
					<ScrollView>
                        <View style={{padding: 10}}>
						    <MarkdownDisplay>{content}</MarkdownDisplay>
                        </View>
					</ScrollView>
				)}
			</View>
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        controls: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 16 },
        tabs: { flexDirection: 'row', borderWidth: 1, borderColor: colors.border, borderRadius: 8 },
        tabButton: { paddingVertical: 8, paddingHorizontal: 16 },
        activeTab: { backgroundColor: colors.primary },
        tabText: { color: colors.text },
        activeTabText: { color: colors.white },
        editorContainer: { flex: 1, borderWidth: 1, borderColor: colors.border, marginHorizontal: 16, marginBottom: 16, borderRadius: 8, backgroundColor: colors.surface },
        textArea: { flex: 1, padding: 10, fontSize: 14, fontFamily: 'monospace', color: colors.text },
    });
};

export default FileEditorPage;