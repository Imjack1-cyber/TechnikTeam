import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, TextInput, ScrollView } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import AdminModal from '../../components/ui/AdminModal';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import TemplateModal from '../../components/admin/checklist_templates/TemplateModal';

const AdminChecklistTemplatesPage = () => {
	const templatesApiCall = useCallback(() => apiClient.get('/admin/checklist-templates'), []);
	const storageItemsApiCall = useCallback(() => apiClient.get('/storage'), []);

	const { data: templates, loading: templatesLoading, error, reload } = useApi(templatesApiCall, { subscribeTo: 'CHECKLIST_TEMPLATE' });
	const { data: allStorageItems, loading: itemsLoading } = useApi(storageItemsApiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingTemplate, setEditingTemplate] = useState(null);
    const [deletingTemplate, setDeletingTemplate] = useState(null);
    const [isSubmittingDelete, setIsSubmittingDelete] = useState(false);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const openModal = (template = null) => {
		setEditingTemplate(template);
		setIsModalOpen(true);
	};

	const confirmDelete = async () => {
        if (!deletingTemplate) return;
        setIsSubmittingDelete(true);
        try {
            const result = await apiClient.delete(`/admin/checklist-templates/${deletingTemplate.id}`);
            if (result.success) {
                addToast('Vorlage gelöscht', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
        finally {
            setIsSubmittingDelete(false);
            setDeletingTemplate(null);
        }
	};
    
    const handleSuccess = () => {
        setIsModalOpen(false);
        setEditingTemplate(null);
        reload();
    };

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>{item.name}</Text>
            <Text style={styles.description}>{item.description}</Text>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Anzahl Items:</Text>
                <Text style={styles.value}>{item.items?.length || 0}</Text>
            </View>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal(item)}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => setDeletingTemplate(item)}><Text style={styles.dangerOutlineButtonText}>Löschen</Text></TouchableOpacity>
            </View>
        </View>
    );

	const loading = templatesLoading || itemsLoading;

	return (
		<View style={styles.container}>
			<View style={styles.headerContainer}>
                <Icon name="tasks" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Checklisten-Vorlagen</Text>
            </View>
			<Text style={styles.subtitle}>Verwalten Sie hier Vorlagen für wiederverwendbare Checklisten.</Text>
            <TouchableOpacity style={[styles.button, styles.successButton, { alignSelf: 'flex-start', marginHorizontal: 16, marginBottom: 16}]} onPress={() => openModal()}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neue Vorlage</Text>
            </TouchableOpacity>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={templates}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{paddingHorizontal: 16}}
            />

			{isModalOpen && !itemsLoading && (
				<TemplateModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={handleSuccess} template={editingTemplate} allStorageItems={allStorageItems || []} />
			)}
            {deletingTemplate && (
                <ConfirmationModal
                    isOpen={!!deletingTemplate}
                    onClose={() => setDeletingTemplate(null)}
                    onConfirm={confirmDelete}
                    title={`Vorlage "${deletingTemplate.name}" löschen?`}
                    message="Diese Aktion kann nicht rückgängig gemacht werden."
                    confirmText="Löschen"
                    confirmButtonVariant="danger"
                    isSubmitting={isSubmittingDelete}
                />
            )}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1, backgroundColor: colors.background },
        contentContainer: { paddingBottom: 16 },
        headerContainer: { padding: 16, flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16 },
        description: { color: colors.textMuted, marginVertical: 8 },
    });
};

export default AdminChecklistTemplatesPage;