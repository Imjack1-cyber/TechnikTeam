import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, ActivityIndicator, StyleSheet } from 'react-native';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import useApi from '../../../hooks/useApi';
import RelatedItemsManager from './RelatedItemsManager';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { Picker } from '@react-native-picker/picker';

const StorageItemModal = ({ isOpen, onClose, onSuccess, item, initialMode = 'edit' }) => {
	const { data: allItems } = useApi(useCallback(() => apiClient.get('/storage'), []));
	const [mode, setMode] = useState(initialMode);
	const [formData, setFormData] = useState({});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	useEffect(() => {
		if (!isOpen) return;
		setMode(initialMode);
		const baseData = item ? { ...item, category: item.category || '' } : { name: '', location: '', quantity: '1', maxQuantity: '1', category: '' };

		if (initialMode === 'defect') {
			baseData.defective_quantity_change = '1';
			baseData.defect_reason_change = '';
			baseData.status = 'DEFECT';
		} else if (initialMode === 'repair') {
			baseData.repaired_quantity = '1';
			baseData.repair_notes = '';
		}
		setFormData(baseData);
	}, [item, initialMode, isOpen]);

	const handleChange = (name, value) => setFormData(prev => ({ ...prev, [name]: value }));

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		// File upload with FormData would need a native library
		try {
			const payload = { ...formData };
			delete payload.imageFile;

			const result = (mode === 'create')
				? await apiClient.post('/storage', payload)
				: await apiClient.put(`/storage/${item.id}`, payload);

			if (result.success) {
				addToast(`Artikel ${mode === 'create' ? 'erstellt' : 'aktualisiert'}.`, 'success');
				onSuccess();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Ein Fehler ist aufgetreten.');
		} finally {
			setIsSubmitting(false);
		}
	};
    
    // Simplified handlers for defect/repair
    const handleStatusUpdateSubmit = async () => {
        setIsSubmitting(true);
		setError('');
        const isRepair = mode === 'repair';
        const payload = isRepair ? 
            { action: 'repair', quantity: parseInt(formData.repaired_quantity, 10), notes: formData.repair_notes } :
            { action: formData.status === 'UNREPAIRABLE' ? 'report_unrepairable' : 'report_defect', quantity: parseInt(formData.defective_quantity_change, 10), reason: formData.defect_reason_change };
        
        try {
			const result = await apiClient.put(`/storage/${item.id}`, payload);
			if (result.success) {
				addToast('Status erfolgreich aktualisiert.', 'success');
				onSuccess();
			} else throw new Error(result.message);
		} catch (err) {
			setError(err.message);
		} finally {
			setIsSubmitting(false);
		}
    };

	const renderContent = () => {
		switch (mode) {
			case 'defect':
				return (
					<View>
						<Text style={styles.label}>Status</Text>
						<Picker selectedValue={formData.status || 'DEFECT'} onValueChange={val => handleChange('status', val)}>
							<Picker.Item label="Defekt melden" value="DEFECT" />
							<Picker.Item label="Nicht reparierbar" value="UNREPAIRABLE" />
						</Picker>
						<Text style={styles.label}>Anzahl</Text>
						<TextInput style={styles.input} value={formData.defective_quantity_change} onChangeText={val => handleChange('defective_quantity_change', val)} keyboardType="number-pad"/>
						<Text style={styles.label}>Grund</Text>
						<TextInput style={[styles.input, styles.textArea]} value={formData.defect_reason_change} onChangeText={val => handleChange('defect_reason_change', val)} multiline/>
						<TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleStatusUpdateSubmit} disabled={isSubmitting}><Text style={styles.buttonText}>Speichern</Text></TouchableOpacity>
					</View>
				);
			case 'repair':
				return (
                    <View>
                        <Text style={styles.label}>Anzahl reparierter Artikel</Text>
                        <TextInput style={styles.input} value={formData.repaired_quantity} onChangeText={val => handleChange('repaired_quantity', val)} keyboardType="number-pad"/>
                        <Text style={styles.label}>Notiz</Text>
                        <TextInput style={[styles.input, styles.textArea]} value={formData.repair_notes} onChangeText={val => handleChange('repair_notes', val)} multiline/>
                        <TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleStatusUpdateSubmit} disabled={isSubmitting}><Text style={styles.buttonText}>Als repariert buchen</Text></TouchableOpacity>
                    </View>
                );
			case 'relations':
				return <RelatedItemsManager item={item} allItems={allItems || []} onSave={onSuccess} onCancel={onClose} />;
			default:
				return (
					<ScrollView>
						<Text style={styles.label}>Name</Text>
						<TextInput style={styles.input} value={formData.name || ''} onChangeText={val => handleChange('name', val)} />
						<Text style={styles.label}>Kategorie</Text>
						<TextInput style={styles.input} value={formData.category || ''} onChangeText={val => handleChange('category', val)} />
						<Text style={styles.label}>Ort</Text>
						<TextInput style={styles.input} value={formData.location || ''} onChangeText={val => handleChange('location', val)} />
                        <View style={{flexDirection: 'row', gap: 8}}>
                            <View style={{flex: 1}}>
                                <Text style={styles.label}>Menge</Text>
                                <TextInput style={styles.input} value={String(formData.quantity || '')} onChangeText={val => handleChange('quantity', val)} keyboardType="number-pad"/>
                            </View>
                            <View style={{flex: 1}}>
                                <Text style={styles.label}>Max. Menge</Text>
                                <TextInput style={styles.input} value={String(formData.maxQuantity || '')} onChangeText={val => handleChange('maxQuantity', val)} keyboardType="number-pad"/>
                            </View>
                        </View>
						<TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit} disabled={isSubmitting}><Text style={styles.buttonText}>Speichern</Text></TouchableOpacity>
                        {mode === 'edit' && <TouchableOpacity style={[styles.button, styles.secondaryButton, {marginTop: 8}]} onPress={() => setMode('relations')}><Text style={styles.buttonText}>Zugehörige Artikel</Text></TouchableOpacity>}
					</ScrollView>
				);
		}
	};
    
	const getTitle = () => {
		switch (mode) {
			case 'create': return 'Neuen Artikel anlegen';
			case 'edit': return `Artikel bearbeiten`;
			case 'defect': return `Defekt-Status`;
			case 'repair': return `Artikel repariert`;
			case 'relations': return `Zugehörige Artikel`;
			default: return 'Lagerartikel';
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={getTitle()}>
			{error && <Text style={styles.errorText}>{error}</Text>}
			{renderContent()}
		</Modal>
	);
};

export default StorageItemModal;