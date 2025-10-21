import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, TextInput, ActivityIndicator, ScrollView, Alert, Platform } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import ScrollableContent from '../../components/ui/ScrollableContent';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import ChangelogModal from '../../components/admin/changelogs/ChangelogModal';
import ViewChangelogModal from '../../components/admin/changelogs/ViewChangelogModal';

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { padding: spacing.md, flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
        cardTitle: { fontSize: typography.h4, fontWeight: 'bold', flexShrink: 1, color: colors.heading },
        cardActions: { flexDirection: 'row', gap: 16 },
        markdownContainerTruncated: { maxHeight: 200, overflow: 'hidden' },
        actionsRow: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            marginTop: 12,
            paddingTop: 12,
            borderTopWidth: 1,
            borderTopColor: colors.border,
        },
        readMoreButton: {
            alignItems: 'center',
        },
        readMoreText: {
            color: colors.primary,
            fontWeight: 'bold',
        },
        modalMarkdownContainer: {
            maxHeight: '80%',
            borderWidth: 1,
            borderColor: colors.border,
            borderRadius: 6,
            marginTop: 12,
        },
    });
};

const AdminChangelogPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/changelogs'), []);
	const { data: changelogs, loading, error, reload } = useApi(apiCall, { subscribeTo: 'CHANGELOG' });
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingChangelog, setEditingChangelog] = useState(null);
    const [viewingChangelog, setViewingChangelog] = useState(null);
    const [deletingChangelog, setDeletingChangelog] = useState(null);
    const [isSubmittingDelete, setIsSubmittingDelete] = useState(false);
    const [expandedIds, setExpandedIds] = useState([]);
    const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const toggleExpand = (id) => {
        setExpandedIds(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
    };

	const openModal = (changelog = null) => {
		setEditingChangelog(changelog);
		setIsModalOpen(true);
	};

	const confirmDelete = async () => {
        if (!deletingChangelog) return;
        setIsSubmittingDelete(true);
        try {
            const result = await apiClient.delete(`/admin/changelogs/${deletingChangelog.id}`);
            if (result.success) {
                addToast('Changelog gelöscht', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
        finally {
            setIsSubmittingDelete(false);
            setDeletingChangelog(null);
        }
	};
    
    const renderItem = ({ item }) => {
        const isVeryLongContent = item.notes.length > 500;
        const isExpanded = expandedIds.includes(item.id);
        const previewContent = isVeryLongContent && !isExpanded ? item.notes.slice(0, 400) + " …" : item.notes;

        return(
            <View style={styles.card}>
                <View style={styles.cardHeader}>
                    <View style={{flex: 1}}>
                        <Text style={styles.cardTitle}>Version {item.version} - {item.title}</Text>
                        <Text style={styles.subtitle}>Veröffentlicht: {new Date(item.releaseDate).toLocaleDateString('de-DE')}</Text>
                    </View>
                    <View style={styles.cardActions}>
                        <TouchableOpacity onPress={() => openModal(item)}><Icon name="edit" size={18} color={colors.textMuted}/></TouchableOpacity>
                        <TouchableOpacity onPress={() => setDeletingChangelog(item)}><Icon name="trash" size={18} color={colors.danger} /></TouchableOpacity>
                    </View>
                </View>
                <View style={isVeryLongContent && !isExpanded ? styles.markdownContainerTruncated : {}}>
                    <MarkdownDisplay style={{ body: { color: colors.text } }}>{previewContent}</MarkdownDisplay>
                </View>
                 {isVeryLongContent && (
                    <View style={styles.actionsRow}>
                        {!isExpanded &&
                            <TouchableOpacity style={styles.readMoreButton} onPress={() => toggleExpand(item.id)}>
                                <Text style={styles.readMoreText}>Mehr anzeigen</Text>
                            </TouchableOpacity>
                        }
                        {isExpanded &&
                             <TouchableOpacity style={styles.readMoreButton} onPress={() => toggleExpand(item.id)}>
                                <Text style={styles.readMoreText}>Weniger anzeigen</Text>
                            </TouchableOpacity>
                        }
                        <TouchableOpacity style={styles.readMoreButton} onPress={() => setViewingChangelog(item)}>
                            <Text style={styles.readMoreText}>Im Fenster öffnen</Text>
                        </TouchableOpacity>
                    </View>
                )}
            </View>
        );
    };

	return (
		<View style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="history" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Changelogs verwalten</Text>
            </View>
			<Text style={styles.subtitle}>Verwalten Sie hier die "Was ist neu?"-Benachrichtigungen.</Text>
            <TouchableOpacity style={[styles.button, styles.successButton, { alignSelf: 'flex-start', marginHorizontal: 16, marginBottom: 16 }]} onPress={() => openModal()}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neuer Eintrag</Text>
            </TouchableOpacity>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			
            <FlatList
                data={changelogs}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{paddingHorizontal: 16}}
            />

			{isModalOpen && <ChangelogModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={() => { setIsModalOpen(false); reload(); }} changelog={editingChangelog} />}
            <ViewChangelogModal changelog={viewingChangelog} onClose={() => setViewingChangelog(null)} />
            {deletingChangelog && (
                <ConfirmationModal
                    isOpen={!!deletingChangelog}
                    onClose={() => setDeletingChangelog(null)}
                    onConfirm={confirmDelete}
                    title={`Changelog "${deletingChangelog.version}" löschen?`}
                    message="Diese Aktion kann nicht rückgängig gemacht werden."
                    confirmText="Löschen"
                    isSubmitting={isSubmittingDelete}
                />
            )}
		</View>
	);
};

export default AdminChangelogPage;