import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, Linking } from 'react-native';
import useApi from '../../hooks/useApi';
import useAdminData from '../../hooks/useAdminData';
import apiClient from '../../services/apiClient';
import KitModal from '../../components/admin/kits/KitModal';
import KitItemsForm from '../../components/admin/kits/KitItemsForm';
import Modal from '../../components/ui/Modal';
import QRCode from 'react-native-qrcode-svg';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';

const KitAccordion = ({ kit, onEdit, onDelete, onItemsUpdate, allStorageItems, storageReady }) => {
	const [isOpen, setIsOpen] = useState(false);
	const [isQrModalOpen, setIsQrModalOpen] = useState(false);
	const packKitUrl = `http://localhost:8081/TechnikTeam/pack-kit/${kit.id}`; // Hardcoded for native
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	return (
		<View style={styles.card}>
			<TouchableOpacity onPress={() => setIsOpen(!isOpen)} style={styles.accordionHeader}>
				<View style={{flexDirection: 'row', alignItems: 'center', flex: 1}}>
                    <Icon name={isOpen ? 'chevron-down' : 'chevron-right'} size={16} />
                    <View style={{marginLeft: 12}}>
                        <Text style={styles.cardTitle}>{kit.name}</Text>
                        <Text style={styles.subtitle}>{kit.description}</Text>
                    </View>
                </View>
                <TouchableOpacity style={[styles.button, {backgroundColor: getThemeColors(theme).primaryLight}]} onPress={() => setIsQrModalOpen(true)}>
                    <Text style={{color: getThemeColors(theme).primary}}>QR-Code</Text>
                </TouchableOpacity>
			</TouchableOpacity>

			{isOpen && (
				<View style={styles.accordionContent}>
					{!storageReady ? <ActivityIndicator /> : (
						<KitItemsForm kit={kit} allStorageItems={allStorageItems} onUpdateSuccess={onItemsUpdate} />
					)}
                    <View style={styles.cardActions}>
                        <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => onEdit(kit)}>
                            <Text style={styles.buttonText}>Bearbeiten</Text>
                        </TouchableOpacity>
                        <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => onDelete(kit)}>
                            <Text style={styles.dangerOutlineButtonText}>Löschen</Text>
                        </TouchableOpacity>
                    </View>
				</View>
			)}

			<Modal isOpen={isQrModalOpen} onClose={() => setIsQrModalOpen(false)} title={`QR-Code für: ${kit.name}`}>
				<View style={{alignItems: 'center', padding: 16}}>
					<QRCode value={packKitUrl} size={256} />
					<Text style={{marginTop: 16}}>Scannen, um die Packliste zu öffnen.</Text>
					<Text style={{color: getThemeColors(theme).primary, marginTop: 8}} onPress={() => Linking.openURL(packKitUrl)}>{packKitUrl}</Text>
				</View>
			</Modal>
		</View>
	);
};

const AdminKitsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/kits'), []);
	const { data: kits, loading: kitsLoading, error: kitsError, reload } = useApi(apiCall);
	const { storageItems, loading: storageItemsLoading, error: storageItemsError } = useAdminData();
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingKit, setEditingKit] = useState(null);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const openModal = (kit = null) => {
		setEditingKit(kit);
		setIsModalOpen(true);
	};

	const handleSuccess = () => {
		setIsModalOpen(false);
        setEditingKit(null);
		reload();
	};

	const handleDelete = (kit) => {
        Alert.alert(`Kit "${kit.name}" löschen?`, "Diese Aktion kann nicht rückgängig gemacht werden.", [
			{ text: 'Abbrechen', style: 'cancel' },
			{ text: 'Löschen', style: 'destructive', onPress: async () => {
				try {
					const result = await apiClient.delete(`/kits/${kit.id}`);
					if (result.success) {
						addToast('Kit erfolgreich gelöscht.', 'success');
						reload();
					} else { throw new Error(result.message); }
				} catch (err) { addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error'); }
			}},
		]);
	};
    
    const renderItem = ({ item }) => (
        <KitAccordion
            kit={item}
            onEdit={openModal}
            onDelete={handleDelete}
            onItemsUpdate={() => addToast('Kit-Inhalt gespeichert.', 'success')}
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
                <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => openModal()}>
                    <Text style={styles.buttonText}>Neues Kit anlegen</Text>
                </TouchableOpacity>
            </View>

            {loading && <ActivityIndicator size="large" style={{marginTop: 20}} />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			
            <FlatList
                data={kits}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{ padding: 16 }}
            />

			{isModalOpen && (
				<KitModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={handleSuccess} kit={editingKit} />
			)}
		</View>
	);
};

export default AdminKitsPage;