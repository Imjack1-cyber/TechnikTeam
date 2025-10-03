import React, { useState, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import StorageItemModal from '../../components/admin/storage/StorageItemModal';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography } from '../../styles/theme';
import ConfirmationModal from '../../components/ui/ConfirmationModal';

const AdminDefectivePage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/storage'), []);
	const { data: allItems, loading, error, reload } = useApi(apiCall, { subscribeTo: ['STORAGE_ITEM', 'DAMAGE_REPORT'] });
	const [modalState, setModalState] = useState({ isOpen: false, item: null, mode: 'defect' });
	const { addToast } = useToast();
    
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);

	const defectiveItems = useMemo(() => allItems?.filter(item => item.defectiveQuantity > 0) || [], [allItems]);

	const openModal = (mode, item) => {
		setModalState({ isOpen: true, item, mode });
	};

	const handleSuccess = () => {
		addToast('Status erfolgreich aktualisiert', 'success');
		setModalState({ isOpen: false, item: null, mode: 'defect' });
		reload();
	};

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <TouchableOpacity onPress={() => navigation.navigate('StorageItemDetails', { itemId: item.id })}>
                <Text style={styles.cardTitle}>{item.name}</Text>
            </TouchableOpacity>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Defekt / Gesamt:</Text>
                <Text style={styles.value}>{item.defectiveQuantity} / {item.quantity}</Text>
            </View>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Grund:</Text>
                <Text style={[styles.value, {flex: 1, textAlign: 'right'}]} numberOfLines={1}>{item.defectReason || '-'}</Text>
            </View>
            <View style={{flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16}}>
                <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => openModal('repair', item)}>
                    <Text style={styles.buttonText}>Repariert</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, {backgroundColor: colors.warning}]} onPress={() => openModal('defect', item)}>
                    <Text style={{color: '#000', fontWeight: '500'}}>Status</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
			<View style={{flexDirection: 'row', alignItems: 'center', padding: 16}}>
                <Icon name="wrench" size={24} style={{color: getThemeColors(theme).heading, marginRight: 12}} />
			    <Text style={styles.title}>Defekte Artikel</Text>
            </View>
			<Text style={styles.subtitle}>Hier sind alle Artikel gelistet, von denen mindestens ein Exemplar als defekt markiert wurde.</Text>

			{loading && <ActivityIndicator size="large" style={{marginTop: 20}}/>}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={defectiveItems}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
                ListEmptyComponent={<View style={styles.card}><Text>Aktuell sind keine Artikel als defekt gemeldet.</Text></View>}
            />

			{modalState.isOpen && (
				<StorageItemModal
					isOpen={modalState.isOpen}
					onClose={() => setModalState({ isOpen: false, item: null, mode: 'defect' })}
					onSuccess={handleSuccess}
					item={modalState.item}
					initialMode={modalState.mode}
				/>
			)}
		</View>
	);
};

export default AdminDefectivePage;