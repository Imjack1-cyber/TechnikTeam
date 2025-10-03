import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, Platform } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import StorageItemModal from '../../components/admin/storage/StorageItemModal';
import Lightbox from '../../components/ui/Lightbox';
import { useToast } from '../../context/ToastContext';
import Modal from '../../components/ui/Modal';
import QRCode from 'react-native-qrcode-svg';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography } from '../../styles/theme';
import ConfirmationModal from '../../components/ui/ConfirmationModal';

const HealthIndicator = ({ item }) => {
	let color = '#28a745'; // success
	if (item.defectiveQuantity > 0) color = '#dc3545'; // danger
	else if (item.maxQuantity > 0 && item.availableQuantity < item.maxQuantity) color = '#ffc107'; // warning
	return <View style={{ width: 10, height: 10, borderRadius: 5, backgroundColor: color }} />;
};

const AdminStoragePage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/storage'), []);
	const { data: items, loading, error, reload } = useApi(apiCall, { subscribeTo: 'STORAGE_ITEM' });
	const [modalState, setModalState] = useState({ isOpen: false, item: null, mode: 'edit' });
	const [lightboxSrc, setLightboxSrc] = useState('');
	const [qrCodeItem, setQrCodeItem] = useState(null);
    const [deletingItem, setDeletingItem] = useState(null);
    const [isSubmittingDelete, setIsSubmittingDelete] = useState(false);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

	const openModal = (mode, item = null) => setModalState({ isOpen: true, item, mode });
	const handleSuccess = () => { setModalState({ isOpen: false, item: null, mode: 'edit' }); reload(); };

	const confirmDelete = async () => {
        if (!deletingItem) return;
        setIsSubmittingDelete(true);
        try {
            const result = await apiClient.delete(`/storage/${deletingItem.id}`);
            if (result.success) {
                addToast('Artikel gelöscht.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error'); }
        finally {
            setIsSubmittingDelete(false);
            setDeletingItem(null);
        }
	};

	const getImagePath = (path) => `${apiClient.getRootUrl()}/api/v1/public/files/images/${path.split('/').pop()}`;

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <View style={styles.cardHeader}>
                <HealthIndicator item={item} />
                <TouchableOpacity onPress={() => navigation.navigate('StorageItemDetails', { itemId: item.id })}>
                    <Text style={styles.cardTitle}>{item.name}</Text>
                </TouchableOpacity>
                {item.imagePath && (
                    <TouchableOpacity onPress={() => setLightboxSrc(getImagePath(item.imagePath))}>
                        <Icon name="camera" size={18} color={getThemeColors(theme).textMuted} />
                    </TouchableOpacity>
                )}
            </View>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Verfügbar:</Text>
                <Text style={styles.value}>{item.availableQuantity}/{item.maxQuantity} {item.defectiveQuantity > 0 && `(${item.defectiveQuantity} def.)`}</Text>
            </View>
            <View style={styles.cardActions}>
                <TouchableOpacity style={styles.actionButton} onPress={() => openModal('edit', item)}><Text>Bearbeiten</Text></TouchableOpacity>
                <TouchableOpacity style={styles.actionButton} onPress={() => openModal('defect', item)}><Text>Defekt</Text></TouchableOpacity>
                {item.defectiveQuantity > 0 && (
                    <TouchableOpacity style={styles.actionButton} onPress={() => openModal('repair', item)}><Text>Repariert</Text></TouchableOpacity>
                )}
                <TouchableOpacity style={styles.actionButton} onPress={() => setQrCodeItem(item)}><Text>QR</Text></TouchableOpacity>
                <TouchableOpacity style={styles.actionButton} onPress={() => setDeletingItem(item)}><Text style={{color: getThemeColors(theme).danger}}>Löschen</Text></TouchableOpacity>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
            <TouchableOpacity style={[styles.button, styles.successButton, {margin: 16}]} onPress={() => openModal('create')}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neuen Artikel anlegen</Text>
            </TouchableOpacity>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={items}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{paddingHorizontal: 16}}
            />

			{modalState.isOpen && <StorageItemModal isOpen={modalState.isOpen} onClose={() => setModalState({isOpen: false, item: null, mode: 'edit'})} onSuccess={handleSuccess} item={modalState.item} initialMode={modalState.mode} />}
			{qrCodeItem && (
				<Modal isOpen={!!qrCodeItem} onClose={() => setQrCodeItem(null)} title={`QR-Code für: ${qrCodeItem.name}`}>
					<View style={{alignItems: 'center', padding: 16}}>
						<QRCode value={`${apiClient.getRootUrl()}/lager/qr-aktion/${qrCodeItem.id}`} size={256} />
						<Text style={{marginTop: 16}}>Scannen für schnelle Aktionen.</Text>
					</View>
				</Modal>
			)}
			{lightboxSrc && <Lightbox src={lightboxSrc} onClose={() => setLightboxSrc('')} />}
            {deletingItem && (
                 <ConfirmationModal
                    isOpen={!!deletingItem}
                    onClose={() => setDeletingItem(null)}
                    onConfirm={confirmDelete}
                    title={`Artikel "${deletingItem.name}" löschen?`}
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
        cardHeader: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 8 },
        cardTitle: { fontSize: typography.h4, fontWeight: 'bold' },
        cardActions: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginTop: 12, justifyContent: 'flex-end' },
        actionButton: { padding: 8, backgroundColor: colors.background, borderRadius: 6 },
    });
};

export default AdminStoragePage;