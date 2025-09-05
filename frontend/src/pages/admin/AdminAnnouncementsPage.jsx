import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, TextInput, ActivityIndicator, ScrollView, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';

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
		<Modal isOpen={isOpen} onClose={onClose} title={announcement ? "Mitteilung bearbeiten" : "Neue Mitteilung erstellen"}>
			<ScrollView>
				{error && <Text style={styles.errorText}>{error}</Text>}
				<Text style={styles.label}>Titel</Text>
				<TextInput style={styles.input} value={title} onChangeText={setTitle} />
				<Text style={styles.label}>Inhalt (Markdown unterstützt)</Text>
				<TextInput style={[styles.input, styles.textArea]} value={content} onChangeText={setContent} multiline />
				<TouchableOpacity style={[styles.button, styles.primaryButton, { marginTop: 16 }]} onPress={handleSubmit} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff"/> : <Text style={styles.buttonText}>Speichern</Text>}
				</TouchableOpacity>
			</ScrollView>
		</Modal>
	);
};

const AdminAnnouncementsPage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/admin/announcements'), []);
	const { data: announcements, loading, error, reload } = useApi(apiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingAnnouncement, setEditingAnnouncement] = useState(null);
	const { addToast } = useToast();

    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const openModal = (announcement = null) => {
		setEditingAnnouncement(announcement);
		setIsModalOpen(true);
	};

	const handleSuccess = () => {
		setIsModalOpen(false);
		setEditingAnnouncement(null);
		reload();
	};

	const handleDelete = (announcement) => {
        Alert.alert(`Mitteilung "${announcement.title}" löschen?`, "", [
            { text: 'Abbrechen', style: 'cancel' },
            { text: 'Löschen', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.delete(`/admin/announcements/${announcement.id}`);
                    if (result.success) {
                        addToast('Mitteilung gelöscht', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Fehler: ${err.message}`, 'error');}
            }}
        ]);
	};
    
    const renderItem = ({ item }) => (
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
                    <TouchableOpacity onPress={() => handleDelete(item)}><Icon name="trash" size={18} color={colors.danger} /></TouchableOpacity>
                </View>
            </View>
            <View style={styles.markdownContainer}>
                <MarkdownDisplay>{item.content}</MarkdownDisplay>
            </View>
        </View>
    );

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
        markdownContainer: { maxHeight: 200, borderWidth: 1, borderColor: colors.border, borderRadius: 6, padding: 8 },
    });
};

export default AdminAnnouncementsPage;