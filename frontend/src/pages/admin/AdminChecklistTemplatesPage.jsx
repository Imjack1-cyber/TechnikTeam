import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, TextInput, ScrollView } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import { Picker } from '@react-native-picker/picker';

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
		if (field === 'storageItemId') currentItem.itemText = null;
		if (field === 'itemText') { currentItem.storageItemId = null; currentItem.quantity = 1; }
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
		<Modal isOpen={isOpen} onClose={onClose} title={template ? 'Vorlage bearbeiten' : 'Neue Vorlage'}>
			<ScrollView>
				{error && <Text style={styles.errorText}>{error}</Text>}
				<Text style={styles.label}>Name der Vorlage</Text><TextInput style={styles.input} value={formData.name} onChangeText={val => setFormData({...formData, name: val})} />
				<Text style={styles.label}>Beschreibung</Text><TextInput style={styles.input} value={formData.description} onChangeText={val => setFormData({...formData, description: val})} multiline />
				<Text style={styles.label}>Checklisten-Punkte</Text>
                {items.map((item, index) => (
                    <View style={styles.itemRow} key={index}>
                        {item.storageItemId !== null ? (
                            <>
                                <View style={{flex: 1, borderWidth: 1, borderColor: colors.border, borderRadius: 8}}><Picker selectedValue={item.storageItemId} onValueChange={val => handleItemChange(index, 'storageItemId', val)}><Picker.Item label="Artikel..." value="" />{allStorageItems.map(si => <Picker.Item key={si.id} label={si.name} value={si.id} />)}</Picker></View>
                                <TextInput value={String(item.quantity)} onChangeText={val => handleItemChange(index, 'quantity', val)} style={styles.quantityInput} keyboardType="number-pad"/>
                            </>
                        ) : (
                            <TextInput style={[styles.input, {flex: 1}]} value={item.itemText || ''} onChangeText={val => handleItemChange(index, 'itemText', val)} placeholder={`Text-Punkt #${index + 1}`} />
                        )}
                        <TouchableOpacity onPress={() => handleRemoveItem(index)}><Icon name="times-circle" size={24} color={colors.danger} /></TouchableOpacity>
                    </View>
                ))}
                <View style={styles.actionButtons}>
                    <TouchableOpacity style={styles.button} onPress={handleAddTextItem}><Text style={styles.buttonText}>Textpunkt</Text></TouchableOpacity>
                    <TouchableOpacity style={styles.button} onPress={handleAddStorageItem}><Text style={styles.buttonText}>Lagerartikel</Text></TouchableOpacity>
                </View>
				<TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={handleSubmit} disabled={isSubmitting}><Text style={styles.buttonText}>Speichern</Text></TouchableOpacity>
			</ScrollView>
		</Modal>
	);
};


const AdminChecklistTemplatesPage = () => {
	const templatesApiCall = useCallback(() => apiClient.get('/admin/checklist-templates'), []);
	const storageItemsApiCall = useCallback(() => apiClient.get('/storage'), []);

	const { data: templates, loading, error, reload } = useApi(templatesApiCall);
	const { data: allStorageItems, loading: itemsLoading } = useApi(storageItemsApiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingTemplate, setEditingTemplate] = useState(null);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };

	const openModal = (template = null) => { setEditingTemplate(template); setIsModalOpen(true); };

	const handleDelete = (template) => {
        Alert.alert(`Vorlage "${template.name}" löschen?`, "", [
            { text: 'Abbrechen', style: 'cancel' },
            { text: 'Löschen', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.delete(`/admin/checklist-templates/${template.id}`);
                    if (result.success) {
                        addToast('Vorlage gelöscht', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
            }},
        ]);
	};
    
    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>{item.name}</Text>
            <Text style={styles.description}>{item.description}</Text>
            <View style={styles.detailRow}><Text style={styles.label}>Anzahl Items:</Text><Text style={styles.value}>{item.items?.length || 0}</Text></View>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal(item)}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => handleDelete(item)}><Text style={styles.dangerOutlineButtonText}>Löschen</Text></TouchableOpacity>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
			<View style={styles.headerContainer}>
                <Icon name="tasks" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Checklisten-Vorlagen</Text>
            </View>
			<Text style={styles.subtitle}>Verwalten Sie hier Vorlagen für wiederverwendbare Checklisten.</Text>
            <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => openModal()}><Icon name="plus" size={16} color="#fff" /><Text style={styles.buttonText}>Neue Vorlage</Text></TouchableOpacity>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={templates}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
            />

			{isModalOpen && !itemsLoading && (
				<TemplateModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={() => { setIsModalOpen(false); reload(); }} template={editingTemplate} allStorageItems={allStorageItems || []} />
			)}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center', padding: 16 },
        headerIcon: { color: colors.heading, marginRight: 12 },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16 },
        description: { color: colors.textMuted, marginVertical: 8 },
        itemRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, marginBottom: spacing.sm },
        quantityInput: { width: 70, height: 48, borderWidth: 1, borderColor: colors.border, borderRadius: 8, paddingHorizontal: spacing.sm, textAlign: 'center' },
    });
};

export default AdminChecklistTemplatesPage;