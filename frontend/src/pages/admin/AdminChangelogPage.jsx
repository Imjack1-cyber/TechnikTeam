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
import DateTimePickerModal from "react-native-modal-datetime-picker";
import { format, parseISO } from 'date-fns';
import AdminModal from '../../components/ui/AdminModal';
import ScrollableContent from '../../components/ui/ScrollableContent';
import Modal from '../../components/ui/Modal';
import ConfirmationModal from '../../components/ui/ConfirmationModal';

const ChangelogModal = ({ isOpen, onClose, onSuccess, changelog }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
	const [formData, setFormData] = useState({
		version: changelog?.version || '',
		releaseDate: changelog?.releaseDate || new Date().toISOString().split('T')[0],
		title: changelog?.title || '',
		notes: changelog?.notes || '',
	});
    const [isDatePickerVisible, setDatePickerVisible] = useState(false);

    useEffect(() => {
        if (changelog) {
            setFormData({ version: changelog.version, releaseDate: changelog.releaseDate, title: changelog.title, notes: changelog.notes });
        } else {
            setFormData({ version: '', releaseDate: new Date().toISOString().split('T')[0], title: '', notes: '' });
        }
    }, [changelog, isOpen]);

    const handleConfirmDate = (date) => {
        setFormData({...formData, releaseDate: format(date, 'yyyy-MM-dd')});
        setDatePickerVisible(false);
    };

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		try {
			const result = changelog
				? await apiClient.put(`/admin/changelogs/${changelog.id}`, formData)
				: await apiClient.post('/admin/changelogs', formData);
			if (result.success) {
				addToast(`Changelog ${changelog ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Fehler beim Speichern');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={changelog ? 'Changelog bearbeiten' : 'Neuen Changelog erstellen'}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Speichern"
        >
            {error && <Text style={styles.errorText}>{error}</Text>}
            <Text style={styles.label}>Version (z.B. 2.1.0)</Text>
            <TextInput style={styles.input} value={formData.version} onChangeText={val => setFormData({...formData, version: val})} />
            <Text style={styles.label}>Titel</Text>
            <TextInput style={styles.input} value={formData.title} onChangeText={val => setFormData({...formData, title: val})} />
            
            <Text style={styles.label}>Veröffentlichungsdatum</Text>
            <TouchableOpacity onPress={() => setDatePickerVisible(true)} style={[styles.input, { justifyContent: 'center' }]}>
                <Text>{format(parseISO(formData.releaseDate), 'dd.MM.yyyy')}</Text>
            </TouchableOpacity>
            <DateTimePickerModal
                isVisible={isDatePickerVisible}
                mode="date"
                onConfirm={handleConfirmDate}
                onCancel={() => setDatePickerVisible(false)}
                date={parseISO(formData.releaseDate)}
            />

            <Text style={styles.label}>Anmerkungen (Markdown)</Text>
            <TextInput style={[styles.input, styles.textArea]} value={formData.notes} onChangeText={val => setFormData({...formData, notes: val})} multiline />
        </AdminModal>
	);
};

const ViewChangelogModal = ({ changelog, onClose }) => {
    if (!changelog) return null;
    const styles = { ...getCommonStyles(), ...pageStyles() };

    return (
        <Modal isOpen={true} onClose={onClose} title={`Version ${changelog.version} - ${changelog.title}`}>
            <Text style={styles.subtitle}>
                Veröffentlicht am {new Date(changelog.releaseDate).toLocaleDateString('de-DE')}
            </Text>
            <ScrollView style={styles.modalMarkdownContainer}>
                <MarkdownDisplay style={{ body: { padding: 12 } }}>
                    {changelog.notes}
                </MarkdownDisplay>
            </ScrollView>
            <TouchableOpacity style={[styles.button, { backgroundColor: '#6c757d', marginTop: 16 }]} onPress={onClose}>
                <Text style={styles.buttonText}>Schließen</Text>
            </TouchableOpacity>
        </Modal>
    );
};

const AdminChangelogPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/changelogs'), []);
	const { data: changelogs, loading, error, reload } = useApi(apiCall);
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
                    <MarkdownDisplay>{previewContent}</MarkdownDisplay>
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
		<ScrollableContent style={styles.container}>
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
		</ScrollableContent>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1 },
        headerContainer: { padding: spacing.md, flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
        cardTitle: { fontSize: typography.h4, fontWeight: 'bold', flexShrink: 1 },
        cardActions: { flexDirection: 'row', gap: 16 },
        markdownContainerTruncated: { maxHeight: 200, overflow: 'hidden' },
        actionsRow: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            marginTop: 12,
            paddingTop: 12,
            borderTopWidth: 1,
            borderTopColor: '#eee',
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
        button: {
            backgroundColor: '#6c757d',
            padding: 12,
            borderRadius: 6,
            alignItems: 'center',
        },
        buttonText: {
            color: '#fff',
            fontWeight: '500',
        },
    });
};

export default AdminChangelogPage;