import React, { useState, useCallback } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView } from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { Picker } from '@react-native-picker/picker';
import Icon from 'react-native-vector-icons/FontAwesome5';
import useApi from '../../../hooks/useApi';

const UploadFileModal = ({ isOpen, onClose, onSuccess }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const { addToast } = useToast();

    const categoriesApiCall = useCallback(() => apiClient.get('/admin/files/categories'), []);
    const { data: categories, loading, error: categoriesError } = useApi(categoriesApiCall);

    const [file, setFile] = useState(null);
    const [categoryId, setCategoryId] = useState('');
    const [newCategoryName, setNewCategoryName] = useState('');
    const [requiredRole, setRequiredRole] = useState('NUTZER');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');

    const handlePickFile = async () => {
        try {
          const res = await DocumentPicker.getDocumentAsync({});
          if (!res.canceled) {
             setFile(res.assets[0]);
          }
        } catch (err) {
            addToast("Fehler beim Auswählen der Datei.", "error");
        }
    };

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');

        const data = new FormData();
        data.append('file', {
            uri: file.uri,
            name: file.name,
            type: file.mimeType,
        });
        data.append('requiredRole', requiredRole);

        if (categoryId === 'NEW') {
            data.append('newCategoryName', newCategoryName);
        } else {
            data.append('categoryId', categoryId);
        }

        try {
            const result = await apiClient.post('/admin/files', data);
            if (result.success) {
                addToast('Datei erfolgreich hochgeladen!', 'success');
                onSuccess();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            setError(err.message || 'Upload fehlgeschlagen.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const isSubmitDisabled = isSubmitting || !file || (categoryId === '' || (categoryId === 'NEW' && !newCategoryName.trim()));

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Neue Datei hochladen">
            <ScrollView>
                {error && <Text style={styles.errorText}>{error}</Text>}
                {categoriesError && <Text style={styles.errorText}>{categoriesError}</Text>}
                
                <View style={styles.formGroup}>
                    <Text style={styles.label}>1. Kategorie auswählen</Text>
                    <Picker selectedValue={categoryId} onValueChange={setCategoryId}>
                        <Picker.Item label="-- Bitte wählen --" value="" />
                        {categories?.map(cat => <Picker.Item key={cat.id} label={cat.name} value={cat.id} />)}
                        <Picker.Item label="** Neue Kategorie erstellen **" value="NEW" />
                    </Picker>
                </View>

                {categoryId === 'NEW' && (
                    <View style={styles.formGroup}>
                        <Text style={styles.label}>Name der neuen Kategorie</Text>
                        <TextInput style={styles.input} value={newCategoryName} onChangeText={setNewCategoryName} />
                    </View>
                )}

                <View style={styles.formGroup}>
                    <Text style={styles.label}>2. Datei auswählen</Text>
                    <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={handlePickFile}>
                        <Icon name="file" size={16} />
                        <Text>Datei auswählen</Text>
                    </TouchableOpacity>
                    {file && <Text style={{marginTop: 8}}>Ausgewählt: {file.name}</Text>}
                </View>

                <View style={styles.formGroup}>
                    <Text style={styles.label}>3. Sichtbarkeit festlegen</Text>
                     <Picker selectedValue={requiredRole} onValueChange={setRequiredRole}>
                        <Picker.Item label="Alle Benutzer" value="NUTZER" />
                        <Picker.Item label="Nur Admins" value="ADMIN" />
                    </Picker>
                </View>

                <TouchableOpacity style={[styles.button, styles.primaryButton, isSubmitDisabled && styles.disabledButton]} onPress={handleSubmit} disabled={isSubmitDisabled}>
                    {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Hochladen</Text>}
                </TouchableOpacity>
            </ScrollView>
        </Modal>
    );
};

export default UploadFileModal;