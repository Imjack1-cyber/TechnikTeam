import React, { useState, useCallback, useRef, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, Linking, Platform, TextInput } from 'react-native';
import useApi from '../../hooks/useApi';
import useAdminData from '../../hooks/useAdminData';
import apiClient from '../../services/apiClient';
import KitModal from '../../components/admin/kits/KitModal';
import KitItemsForm from '../../components/admin/kits/KitItemsForm';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import ShareModal from '../../components/ui/ShareModal';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import AccordionSection from '../../components/ui/AccordionSection';

const KitListItem = ({ kit, onEdit, onDelete, onItemsUpdate, allStorageItems, storageReady }) => {
	const [isShareModalOpen, setIsShareModalOpen] = useState(false);

    const getPackKitUrl = () => {
        const baseUrl = apiClient.getRootUrl() || (typeof window !== 'undefined' ? window.location.origin : '');
        return `${baseUrl}/pack-kit/${kit.id}`;
    };
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	return (
        <>
            <AccordionSection title={kit.name}>
                {!storageReady ? <ActivityIndicator /> : (
                    <KitItemsForm kit={kit} allStorageItems={allStorageItems} onUpdateSuccess={onItemsUpdate} />
                )}
                <View style={styles.cardActions}>
                    <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => setIsShareModalOpen(true)}>
                        <Icon name="qrcode" size={14} color={colors.white} />
                        <Text style={styles.buttonText}> QR-Code</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => onEdit(kit)}>
                        <Icon name="edit" size={14} color={colors.white} />
                        <Text style={styles.buttonText}> Bearbeiten</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => onDelete(kit)}>
                         <Icon name="trash" size={14} color={colors.danger} />
                        <Text style={styles.dangerOutlineButtonText}> Löschen</Text>
                    </TouchableOpacity>
                </View>
            </AccordionSection>
            
            {isShareModalOpen && (
                <ShareModal
                    isOpen={isShareModalOpen}
                    onClose={() => setIsShareModalOpen(false)}
                    isCreatable={false}
                    itemType="kit"
                    itemId={kit.id}
                    itemName={kit.name}
                    shareUrl={getPackKitUrl()}
                />
            )}
        </>
	);
};

const AdminKitsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/kits'), []);
	const { data: kits, loading: kitsLoading, error: kitsError, reload } = useApi(apiCall, { subscribeTo: 'KIT' });
	const { storageItems, loading: storageItemsLoading, error: storageItemsError } = useAdminData();
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingKit, setEditingKit] = useState(null);
    const [deletingKit, setDeletingKit] = useState(null);
    const [isSubmittingDelete, setIsSubmittingDelete] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

    const filteredKits = useMemo(() => {
        if (!kits) return [];
        return kits.filter(kit => kit.name.toLowerCase().includes(searchTerm.toLowerCase()));
    }, [kits, searchTerm]);

	const openModal = (kit = null) => {
		setEditingKit(kit);
		setIsModalOpen(true);
	};

	const handleSuccess = () => {
		setIsModalOpen(false);
        setEditingKit(null);
		reload();
	};

    const confirmDelete = async () => {
        if (!deletingKit) return;
        setIsSubmittingDelete(true);
        try {
            const result = await apiClient.delete(`/kits/${deletingKit.id}`);
            if (result.success) {
                addToast('Kit erfolgreich gelöscht.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error'); }
        finally {
            setIsSubmittingDelete(false);
            setDeletingKit(null);
        }
    };
    
    const renderItem = ({ item }) => (
        <KitListItem
            kit={item}
            onEdit={openModal}
            onDelete={() => setDeletingKit(item)}
            onItemsUpdate={reload}
            allStorageItems={storageItems || []}
            storageReady={!storageItemsLoading}
        />
    );

	const loading = kitsLoading || storageItemsLoading;
	const error = kitsError || storageItemsError;

	return (
		<View style={styles.container}>
			<View style={styles.contentContainer}>
                <Text style={styles.title}><Icon name="box-open" size={24} /> Kit-Verwaltung</Text>
                <Text style={styles.subtitle}>Verwalten Sie hier wiederverwendbare Material-Zusammenstellungen.</Text>
                <TouchableOpacity style={[styles.button, styles.successButton, {alignSelf: 'flex-start'}]} onPress={() => openModal()}>
                    <Icon name="plus" size={16} color="#fff"/>
                    <Text style={styles.buttonText}>Neues Kit anlegen</Text>
                </TouchableOpacity>
                 <TextInput
                    style={[styles.input, {marginTop: spacing.md}]}
                    placeholder="Kit suchen..."
                    value={searchTerm}
                    onChangeText={setSearchTerm}
                />
            </View>

            {loading && <ActivityIndicator size="large" style={{marginTop: 20}} />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			
            <FlatList
                data={filteredKits}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{ padding: 16 }}
            />

			{isModalOpen && (
				<KitModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={handleSuccess} kit={editingKit} />
			)}
            {deletingKit && (
                <ConfirmationModal
                    isOpen={!!deletingKit}
                    onClose={() => setDeletingKit(null)}
                    onConfirm={confirmDelete}
                    title={`Kit "${deletingKit.name}" löschen?`}
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
        accordionHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', padding: 0 },
        accordionContent: { paddingTop: spacing.md, borderTopWidth: 1, borderColor: colors.border, marginTop: spacing.md },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: spacing.sm, marginTop: spacing.md },
    });
};

export default AdminKitsPage;