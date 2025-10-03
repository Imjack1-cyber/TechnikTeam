import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, TextInput, Alert } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import WikiPageModal from '../../components/admin/wiki/WikiPageModal';
import ScrollableContent from '../../components/ui/ScrollableContent';

const WikiTreeNode = ({ name, node, onSelect, selectedId, level = 0 }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);
    const [isOpen, setIsOpen] = useState(true);

    if (node.id) { // It's a file
        return (
            <TouchableOpacity onPress={() => onSelect(node)} style={[styles.treeItem, { paddingLeft: level * spacing.md }, selectedId === node.id && styles.activeTreeItem]}>
                <Icon name="file-alt" style={styles.treeIcon} />
                <Text style={{color: colors.text}}>{name}</Text>
            </TouchableOpacity>
        );
    }

    // It's a directory
    return (
        <View>
            <TouchableOpacity onPress={() => setIsOpen(!isOpen)} style={[styles.treeItem, { paddingLeft: level * spacing.md }]}>
                <Icon name={isOpen ? "folder-open" : "folder"} solid style={styles.treeIcon} />
                <Text style={{fontWeight: 'bold', color: colors.text}}>{name}</Text>
            </TouchableOpacity>
            {isOpen && (
                <View>
                    {Object.entries(node).map(([childName, childNode]) => (
                        <WikiTreeNode key={childName} name={childName} node={childNode} onSelect={onSelect} selectedId={selectedId} level={level + 1} />
                    ))}
                </View>
            )}
        </View>
    );
};

const AdminWikiPage = () => {
	const treeApiCall = useCallback(() => apiClient.get('/wiki'), []);
	const { data: wikiTree, loading, error, reload } = useApi(treeApiCall, { subscribeTo: 'WIKI' });
	const [selectedEntry, setSelectedEntry] = useState(null);
	const [isEditing, setIsEditing] = useState(false);
	const [editContent, setEditContent] = useState('');
	const [isModalOpen, setIsModalOpen] = useState(false);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

	const handleSelectEntry = async (entry) => {
		try {
			const result = await apiClient.get(`/wiki/${entry.id}`);
			if (result.success) {
				setSelectedEntry(result.data);
				setEditContent(result.data.content);
				setIsEditing(false);
			}
		} catch (err) { addToast(`Fehler beim Laden: ${err.message}`, 'error'); }
	};

	const handleSave = async () => {
		if (!selectedEntry) return;
		try {
			const result = await apiClient.put(`/wiki/${selectedEntry.id}`, { content: editContent });
			if (result.success) {
				addToast('Seite gespeichert', 'success');
				await handleSelectEntry(selectedEntry);
			} else { throw new Error(result.message); }
		} catch (err) { addToast(`Fehler beim Speichern: ${err.message}`, 'error'); }
	};
    
	const handleDelete = () => {
        if (!selectedEntry) return;
        Alert.alert(`Seite "${selectedEntry.filePath}" löschen?`, "", [
            { text: 'Abbrechen', style: 'cancel' },
            { text: 'Löschen', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.delete(`/wiki/${selectedEntry.id}`);
                    if (result.success) {
                        addToast('Seite gelöscht', 'success');
                        setSelectedEntry(null);
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
            }}
        ]);
	};
    
	if (loading) return <View style={styles.centered}><ActivityIndicator size="large"/></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;

    if (!selectedEntry) {
        return (
            <View style={styles.container}>
                <View style={styles.sidebarHeader}>
                    <Text style={styles.title}>Wiki-Verzeichnis</Text>
                    <TouchableOpacity onPress={() => setIsModalOpen(true)}><Icon name="plus" size={20} /></TouchableOpacity>
                </View>
                <ScrollableContent contentContainerStyle={styles.contentContainer}>
                    {wikiTree && Object.entries(wikiTree).map(([name, node]) => (
                        <WikiTreeNode key={name} name={name} node={node} onSelect={handleSelectEntry} selectedId={null} />
                    ))}
                </ScrollableContent>
                <WikiPageModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={() => {setIsModalOpen(false); reload();}} parentPath={""}/>
            </View>
        );
    }

	return (
		<View style={styles.container}>
            <View style={styles.contentHeader}>
                <TouchableOpacity onPress={() => setSelectedEntry(null)}><Icon name="arrow-left" size={20} /></TouchableOpacity>
                <Text style={styles.contentTitle} numberOfLines={1}>{selectedEntry.filePath}</Text>
                <TouchableOpacity onPress={handleDelete}><Icon name="trash" size={20} color={getThemeColors(theme).danger} /></TouchableOpacity>
            </View>
			
            <View style={styles.controls}>
                {isEditing ? (
                    <>
                        <TouchableOpacity onPress={handleSave} style={[styles.button, styles.successButton]}><Text style={styles.buttonText}>Speichern</Text></TouchableOpacity>
                        <TouchableOpacity onPress={() => setIsEditing(false)} style={[styles.button, styles.secondaryButton]}><Text style={styles.buttonText}>Abbrechen</Text></TouchableOpacity>
                    </>
                ) : (
                    <TouchableOpacity onPress={() => setIsEditing(true)} style={[styles.button, styles.secondaryButton]}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>
                )}
            </View>
			
            {isEditing ? (
                <TextInput value={editContent} onChangeText={setEditContent} multiline style={styles.textArea} />
            ) : (
                <ScrollableContent style={{padding: spacing.md}}><MarkdownDisplay>{selectedEntry.content}</MarkdownDisplay></ScrollableContent>
            )}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        sidebarHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: spacing.md },
        treeItem: { flexDirection: 'row', alignItems: 'center', paddingVertical: spacing.sm },
        activeTreeItem: { backgroundColor: colors.primaryLight },
        treeIcon: { marginRight: spacing.sm, color: colors.textMuted, width: 20 },
        contentHeader: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, padding: spacing.md, borderBottomWidth: 1, borderColor: colors.border },
        contentTitle: { fontSize: typography.h4, fontWeight: 'bold', flex: 1 },
        controls: { flexDirection: 'row', gap: spacing.sm, padding: spacing.md },
        textArea: { flex: 1, padding: spacing.md, fontSize: 14, fontFamily: 'monospace', color: colors.text, textAlignVertical: 'top', borderWidth: 1, borderColor: colors.border, borderRadius: 8 },
    });
};

export default AdminWikiPage;