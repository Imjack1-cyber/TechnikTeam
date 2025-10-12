import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ScrollView } from 'react-native';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import AdminModal from '../../ui/AdminModal';
import { getThemeColors, spacing } from '../../../styles/theme';
import { Picker } from '@react-native-picker/picker';
import Icon from 'react-native-vector-icons/FontAwesome5';

const TemplateModal = ({ isOpen, onClose, onSuccess, template, allStorageItems }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const [items, setItems] = useState([]);
	const [formData, setFormData] = useState({ name: '', description: '' });
	const { addToast } = useToast();

	useEffect(() => {
		if (isOpen) {
            setFormData({ name: template?.name || '', description: template?.description || '' });
			const initialItems = template?.items?.map(i => ({ itemText: i.itemText, storageItemId: i.storageItemId, quantity: i.quantity || 1 })) || [{ itemText: '', storageItemId: null, quantity: 1 }];
			setItems(initialItems.length > 0 ? initialItems : [{ itemText: '', storageItemId: null, quantity: 1 }]);
		}
	}, [isOpen, template]);

	const handleItemChange = (index, field, value) => {
		const newItems = [...items];
		let currentItem = { ...newItems[index], [field]: value };

		if (field === 'storageItemId') {
			currentItem.itemText = null;
			const selectedItem = allStorageItems.find(si => si.id === parseInt(value));
			if (selectedItem && selectedItem.maxQuantity > 0 && currentItem.quantity > selectedItem.maxQuantity) {
				currentItem.quantity = 1;
			}
		}
		if (field === 'itemText') {
			currentItem.storageItemId = null;
			currentItem.quantity = 1;
		}
		if (field === 'quantity') {
			const selectedItem = allStorageItems.find(si => si.id === parseInt(currentItem.storageItemId));
			let numValue = parseInt(value, 10);
			if (isNaN(numValue) || numValue < 1) {
				numValue = 1;
			}
			if (selectedItem && selectedItem.maxQuantity > 0) {
				currentItem.quantity = Math.min(numValue, selectedItem.maxQuantity);
			} else {
				currentItem.quantity = numValue;
			}
		}

		newItems[index] = currentItem;
		setItems(newItems);
	};

	const handleAddTextItem = () => setItems([...items, { itemText: '', storageItemId: null, quantity: 1 }]);
	const handleAddStorageItem = () => setItems([...items, { itemText: null, storageItemId: '', quantity: 1 }]);
	const handleRemoveItem = (index) => setItems(items.filter((_, i) => i !== index));

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		const finalItems = items.filter(item => (item.itemText && item.itemText.trim() !== '') || (item.storageItemId)).map(item => ({ itemText: item.storageItemId ? null : item.itemText, storageItemId: item.storageItemId ? parseInt(item.storageItemId, 10) : null, quantity: item.storageItemId ? parseInt(item.quantity, 10) : null }));
		const data = { ...formData, items: finalItems };

		try {
			const result = template ? await apiClient.put(`/admin/checklist-templates/${template.id}`, data) : await apiClient.post('/admin/checklist-templates', data);
			if (result.success) {
				addToast(`Vorlage erfolgreich ${template ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Fehler beim Speichern');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<AdminModal isOpen={isOpen} onClose={onClose} title={template ? 'Vorlage bearbeiten' : 'Neue Vorlage'} onSubmit={handleSubmit} isSubmitting={isSubmitting}>
			<ScrollView>
				{error && <Text style={styles.errorText}>{error}</Text>}
				<Text style={styles.label}>Name der Vorlage</Text><TextInput style={styles.input} value={formData.name} onChangeText={val => setFormData({...formData, name: val})} placeholderTextColor={colors.textMuted}/>
				<Text style={styles.label}>Beschreibung</Text><TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => setFormData({...formData, description: val})} multiline placeholderTextColor={colors.textMuted}/>
				<Text style={styles.label}>Checklisten-Punkte</Text>
                {items.map((item, index) => (
                    <View style={styles.itemRow} key={index}>
                        {item.storageItemId !== null ? (
                            <>
                                <View style={{flex: 1, borderWidth: 1, borderColor: colors.border, borderRadius: 8}}><Picker selectedValue={item.storageItemId} onValueChange={val => handleItemChange(index, 'storageItemId', val)} itemStyle={{color: colors.text}}><Picker.Item label="Artikel..." value="" />{allStorageItems.map(si => <Picker.Item key={si.id} label={si.name} value={si.id} />)}</Picker></View>
                                <TextInput value={String(item.quantity)} onChangeText={val => handleItemChange(index, 'quantity', val)} style={styles.quantityInput} keyboardType="number-pad"/>
                            </>
                        ) : (
                            <TextInput style={[styles.input, {flex: 1}]} value={item.itemText || ''} onChangeText={val => handleItemChange(index, 'itemText', val)} placeholder={`Text-Punkt #${index + 1}`} placeholderTextColor={colors.textMuted}/>
                        )}
                        <TouchableOpacity onPress={() => handleRemoveItem(index)}><Icon name="times-circle" size={24} color={colors.danger} /></TouchableOpacity>
                    </View>
                ))}
                <View style={styles.actionButtons}>
                    <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={handleAddTextItem}><Text style={styles.buttonText}>Textpunkt</Text></TouchableOpacity>
                    <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={handleAddStorageItem}><Text style={styles.buttonText}>Lagerartikel</Text></TouchableOpacity>
                </View>
			</ScrollView>
		</AdminModal>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        itemRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, marginBottom: spacing.sm },
        quantityInput: { width: 70, height: 48, borderWidth: 1, borderColor: colors.border, borderRadius: 8, paddingHorizontal: spacing.sm, textAlign: 'center', color: colors.text },
        actionButtons: {
            flexDirection: 'row',
            gap: spacing.sm,
            marginTop: spacing.sm,
            marginBottom: spacing.md
        }
    });
};

export default TemplateModal;