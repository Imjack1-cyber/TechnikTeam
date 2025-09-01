import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { Picker } from '@react-native-picker/picker';
import BouncyCheckbox from "react-native-bouncy-checkbox";
import { MultipleSelectList } from 'react-native-dropdown-select-list';

const DocumentationModal = ({ isOpen, onClose, onSuccess, doc, allDocs }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
    const [formData, setFormData] = useState({
        pageKey: '', title: '', category: '', pagePath: '', features: '', relatedPages: '[]', adminOnly: false,
    });

    useEffect(() => {
        if (doc) {
            setFormData({
                pageKey: doc.pageKey || '',
                title: doc.title || '',
                category: doc.category || '',
                pagePath: doc.pagePath || '',
                features: doc.features || '',
                relatedPages: doc.relatedPages || '[]',
                adminOnly: doc.adminOnly || false,
            });
        } else {
            setFormData({ pageKey: '', title: '', category: 'Allgemein', pagePath: '/', features: '', relatedPages: '[]', adminOnly: false });
        }
    }, [doc]);

    const handleChange = (name, value) => {
        setFormData(prev => ({ ...prev, [name]: value }));
    };
    
    const handleRelatedPagesChange = (selectedKeys) => {
        handleChange('relatedPages', JSON.stringify(selectedKeys));
    };

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');
        try {
            const result = doc
                ? await apiClient.put(`/admin/documentation/${doc.id}`, formData)
                : await apiClient.post('/admin/documentation', formData);
            if (result.success) {
                addToast('Dokumentation gespeichert.', 'success');
                onSuccess();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            setError(err.message || 'Ein Fehler ist aufgetreten.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const relatedPagesOptions = allDocs.map(d => ({ key: d.pageKey, value: d.title }));
    const selectedRelatedKeys = JSON.parse(formData.relatedPages || '[]');

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={doc ? 'Hilfeseite bearbeiten' : 'Neue Hilfeseite'}>
            <ScrollView>
                {error && <Text style={styles.errorText}>{error}</Text>}
                <Text style={styles.label}>Titel</Text>
                <TextInput style={styles.input} value={formData.title} onChangeText={val => handleChange('title', val)} />
                <Text style={styles.label}>Seiten-Schlüssel (pageKey)</Text>
                <TextInput style={styles.input} value={formData.pageKey} onChangeText={val => handleChange('pageKey', val)} autoCapitalize="none" />
                <Text style={styles.label}>Kategorie</Text>
                <TextInput style={styles.input} value={formData.category} onChangeText={val => handleChange('category', val)} />
                <Text style={styles.label}>Features (Markdown)</Text>
                <TextInput style={[styles.input, styles.textArea]} value={formData.features} onChangeText={val => handleChange('features', val)} multiline />
                <Text style={styles.label}>Verwandte Seiten</Text>
                <MultipleSelectList 
                    setSelected={handleRelatedPagesChange} 
                    data={relatedPagesOptions} 
                    save="key"
                    label="Seiten"
                    placeholder="Seiten auswählen"
                    searchPlaceholder="Suchen"
                    boxStyles={styles.input}
                    defaultOptions={relatedPagesOptions.filter(opt => selectedRelatedKeys.includes(opt.key))}
                />
                <View style={{flexDirection: 'row', alignItems: 'center', marginVertical: 16}}>
                    <BouncyCheckbox isChecked={formData.adminOnly} onPress={isChecked => handleChange('adminOnly', isChecked)} />
                    <Text>Nur für Admins sichtbar</Text>
                </View>
                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit} disabled={isSubmitting}>
                    {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Speichern</Text>}
                </TouchableOpacity>
            </ScrollView>
        </Modal>
    );
};

export default DocumentationModal;