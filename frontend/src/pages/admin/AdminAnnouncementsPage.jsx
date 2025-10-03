import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, TextInput, ActivityIndicator, ScrollView, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import AdminModal from '../../components/ui/AdminModal';
import ScrollableContent from '../../components/ui/ScrollableContent';
import Modal from '../../components/ui/Modal';
import ConfirmationModal from '../../components/ui/ConfirmationModal';

const AnnouncementModal = ({ isOpen, onClose, onSuccess, announcement }) => {
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');

    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

    useEffect(() => {
        if(announcement) {
            setTitle(announcement.title);
            setContent(announcement.content);
        } else {
            setTitle('');
            setContent('');
        }
    }, [announcement]);

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		const data = { title, content };

		try {
			const result = announcement
				? await apiClient.put(`/admin/announcements/${announcement.id}`, data)
				: await apiClient.post('/admin/announcements', data);

			if (result.success) {
				addToast(`Mitteilung erfolgreich ${announcement ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Speichern fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
        <AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={announcement ? "Mitteilung bearbeiten" : "Neue Mitteilung erstellen"}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Speichern"
            submitButtonVariant="primary"
        >
            {error && <Text style={styles.errorText}>{error}</Text>}
            <Text style={styles.label}>Titel</Text>
            <TextInput style={styles.input} value={title} onChangeText={setTitle} />
            <Text style={styles.label}>Inhalt (Markdown unterstützt)</Text>
            <TextInput style={[styles.input, styles.textArea]} value={content} onChangeText={setContent} multiline />
        </AdminModal>
	);
};

const ViewAnnouncementModal = ({ announcement, onClose }) => {
    if (!announcement) return null;
    const styles = { ...getCommonStyles(), ...pageStyles() };

    return (
        <Modal isOpen={true} onClose={onClose} title={announcement.title}>
            <Text style={styles.subtitle}>
                Gepostet von <Text style={{ fontWeight: 'bold' }}>{announcement.authorUsername}</Text> am {new Date(announcement.createdAt).toLocaleDateString('de-DE')}
            </Text>
            <ScrollView style={styles.modalMarkdownContainer}>
                <MarkdownDisplay style={{ body: { padding: 12 } }}>
                    {announcement.content}
                </MarkdownDisplay>
            </ScrollView>
            <TouchableOpacity style={[styles.button, { backgroundColor: '#6c757d', marginTop: 16 }]} onPress={onClose}>
                <Text style={styles.buttonText}>Schließen</Text>
            </TouchableOpacity>
        </Modal>
    );
};

const AdminAnnouncementsPage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/admin/announcements'), []);
	const { data: announcements, loading, error, reload } = useApi(apiCall, { subscribeTo: 'ANNOUNCEMENT' });
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingAnnouncement, setEditingAnnouncement] = useState(null);
    const [viewingAnnouncement, setViewingAnnouncement] = useState(null);
    const [deletingAnnouncement, setDeletingAnnouncement] = useState(null);
    const [isSubmittingDelete, setIsSubmittingDelete] = useState(false);
    const [expandedIds, setExpandedIds] = useState([]);
	const { addToast } = useToast();

    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const toggleExpand = (id) => {
        setExpandedIds(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
    };

	const openModal = (announcement = null) => {
		setEditingAnnouncement(announcement);
		setIsModalOpen(true);
	};

	const handleSuccess = () => {
		setIsModalOpen(false);
		setEditingAnnouncement(null);
		reload();
	};

	const confirmDelete = async () => {
        if (!deletingAnnouncement) return;
        setIsSubmittingDelete(true);
        try {
            const result = await apiClient.delete(`/admin/announcements/${deletingAnnouncement.id}`);
            if (result.success) {
                addToast('Mitteilung gelöscht', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Fehler: ${err.message}`, 'error');}
        finally {
            setIsSubmittingDelete(false);
            setDeletingAnnouncement(null);
        }
	};
    
    const renderItem = ({ item }) => {
        const isVeryLongContent = item.content.length > 500;
        const isExpanded = expandedIds.includes(item.id);
        const previewContent = isVeryLongContent && !isExpanded ? item.content.slice(0, 400) + " …" : item.content;

        return (
            <View style={styles.card}>
                <View style={styles.cardHeader}>
                    <View style={{flex: 1}}>
                        <Text style={styles.cardTitle}>{item.title}</Text>
                        <Text style={styles.subtitle}>
                            Von <Text style={{fontWeight: 'bold'}} onPress={() => navigation.navigate('UserProfile', { userId: item.authorUserId })}>{item.authorUsername}</Text> am {new Date(item.createdAt).toLocaleDateString('de-DE')}
                        </Text>
                    </View>
                    <View style={styles.cardActions}>
                        <TouchableOpacity onPress={() => openModal(item)}><Icon name="edit" size={18} color={colors.textMuted}/></TouchableOpacity>
                        <TouchableOpacity onPress={() => setDeletingAnnouncement(item)}><Icon name="trash" size={18} color={colors.danger} /></TouchableOpacity>
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
                        <TouchableOpacity style={styles.readMoreButton} onPress={() => setViewingAnnouncement(item)}>
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
				<Icon name="thumbtack" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Anschlagbrett verwalten</Text>
			</View>
            <TouchableOpacity style={[styles.button, styles.successButton, styles.createButton]} onPress={() => openModal()}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neue Mitteilung</Text>
            </TouchableOpacity>

			{loading && <ActivityIndicator size="large" style={{marginTop: 20}} />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={announcements}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
            />

			{isModalOpen && (
				<AnnouncementModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={handleSuccess} announcement={editingAnnouncement} />
			)}
            <ViewAnnouncementModal announcement={viewingAnnouncement} onClose={() => setViewingAnnouncement(null)} />
            {deletingAnnouncement && (
                <ConfirmationModal
                    isOpen={!!deletingAnnouncement}
                    onClose={() => setDeletingAnnouncement(null)}
                    onConfirm={confirmDelete}
                    title={`Mitteilung "${deletingAnnouncement.title}" löschen?`}
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
        headerContainer: { flexDirection: 'row', alignItems: 'center', padding: 16 },
        headerIcon: { color: colors.heading, marginRight: 12 },
        createButton: { flexDirection: 'row', gap: 8, alignSelf: 'flex-start', marginHorizontal: 16, marginBottom: 16 },
        cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
        cardTitle: { fontSize: typography.h4, fontWeight: 'bold', color: colors.heading, flexShrink: 1 },
        subtitle: { fontSize: typography.caption, color: colors.textMuted },
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
    });
};

export default AdminAnnouncementsPage;