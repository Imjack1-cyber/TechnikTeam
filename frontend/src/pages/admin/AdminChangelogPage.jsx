import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, TextInput, ActivityIndicator, ScrollView, Alert } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';

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

    useEffect(() => {
        if (changelog) {
            setFormData({ version: changelog.version, releaseDate: changelog.releaseDate, title: changelog.title, notes: changelog.notes });
        } else {
            setFormData({ version: '', releaseDate: new Date().toISOString().split('T')[0], title: '', notes: '' });
        }
    }, [changelog, isOpen]);

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
		<Modal isOpen={isOpen} onClose={onClose} title={changelog ? 'Changelog bearbeiten' : 'Neuen Changelog erstellen'}>
			<ScrollView>
				{error && <Text style={styles.errorText}>{error}</Text>}
				<Text style={styles.label}>Version (z.B. 2.1.0)</Text>
				<TextInput style={styles.input} value={formData.version} onChangeText={val => setFormData({...formData, version: val})} />
				<Text style={styles.label}>Titel</Text>
				<TextInput style={styles.input} value={formData.title} onChangeText={val => setFormData({...formData, title: val})} />
				<Text style={styles.label}>Veröffentlichungsdatum (JJJJ-MM-TT)</Text>
				<TextInput style={styles.input} value={formData.releaseDate} onChangeText={val => setFormData({...formData, releaseDate: val})} />
				<Text style={styles.label}>Anmerkungen (Markdown)</Text>
				<TextInput style={[styles.input, styles.textArea]} value={formData.notes} onChangeText={val => setFormData({...formData, notes: val})} multiline />
				<TouchableOpacity style={[styles.button, styles.primaryButton, { marginTop: 16 }]} onPress={handleSubmit} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff"/> : <Text style={styles.buttonText}>Speichern</Text>}
				</TouchableOpacity>
			</ScrollView>
		</Modal>
	);
};

const AdminChangelogPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/changelogs'), []);
	const { data: changelogs, loading, error, reload } = useApi(apiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingChangelog, setEditingChangelog] = useState(null);
    const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const openModal = (changelog = null) => {
		setEditingChangelog(changelog);
		setIsModalOpen(true);
	};

	const handleDelete = (changelog) => {
        Alert.alert(`Changelog löschen?`, `Version "${changelog.version}" wirklich löschen?`, [
            { text: 'Abbrechen', style: 'cancel'},
            { text: 'Löschen', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.delete(`/admin/changelogs/${changelog.id}`);
                    if (result.success) {
                        addToast('Changelog gelöscht', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
            }}
        ]);
	};

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <View style={styles.cardHeader}>
                <View style={{flex: 1}}>
                    <Text style={styles.cardTitle}>Version {item.version} - {item.title}</Text>
                    <Text style={styles.subtitle}>Veröffentlicht: {new Date(item.releaseDate).toLocaleDateString('de-DE')}</Text>
                </View>
                <View style={styles.cardActions}>
                    <TouchableOpacity onPress={() => openModal(item)}><Icon name="edit" size={18} color={colors.textMuted}/></TouchableOpacity>
                    <TouchableOpacity onPress={() => handleDelete(item)}><Icon name="trash" size={18} color={colors.danger} /></TouchableOpacity>
                </View>
            </View>
            <View style={styles.markdownContainer}>
                <MarkdownDisplay>{item.notes}</MarkdownDisplay>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
			<View style={styles.headerContainer}>
                <Icon name="history" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Changelogs verwalten</Text>
            </View>
			<Text style={styles.subtitle}>Verwalten Sie hier die "Was ist neu?"-Benachrichtigungen.</Text>
            <TouchableOpacity style={[styles.button, styles.successButton, { alignSelf: 'flex-start' }]} onPress={() => openModal()}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neuer Eintrag</Text>
            </TouchableOpacity>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			
            <FlatList
                data={changelogs}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
            />

			{isModalOpen && <ChangelogModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={() => { setIsModalOpen(false); reload(); }} changelog={editingChangelog} />}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { padding: spacing.md, flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
        cardTitle: { fontSize: typography.h4, fontWeight: 'bold', flexShrink: 1 },
        cardActions: { flexDirection: 'row', gap: 16 },
        markdownContainer: { maxHeight: 200, borderWidth: 1, borderColor: colors.border, borderRadius: 6, padding: 8 },
    });
};

export default AdminChangelogPage;