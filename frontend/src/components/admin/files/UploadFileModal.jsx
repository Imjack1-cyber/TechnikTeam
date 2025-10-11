import React, { useState, useCallback } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView, Platform } from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import Modal from '../../../components/ui/Modal';
import apiClient, { MAX_FILE_SIZE_BYTES } from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors } from '../../../styles/theme';
import { Picker } from '@react-native-picker/picker';
import Icon from 'react-native-vector-icons/FontAwesome5';
import useApi from '../../../hooks/useApi';
import { useTransferStore } from '../../../store/transferStore';
import { v4 as uuidv4 } from 'uuid';
import TransferButton from '../../../components/ui/TransferButton';

const UploadFileModal = ({ isOpen, onClose, onSuccess, formatFileSize }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const { addToast } = useToast();
    const { addTransfer, updateTransfer } = useTransferStore();

    const categoriesApiCall = useCallback(() => apiClient.get('/admin/files/categories'), []);
    const { data: categories, loading, error: categoriesError } = useApi(categoriesApiCall);

    const [file, setFile] = useState(null); // Will be the asset from DocumentPicker
    const [categoryId, setCategoryId] = useState('');
    const [newCategoryName, setNewCategoryName] = useState('');
    const [requiredRole, setRequiredRole] = useState('NUTZER');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [transferId, setTransferId] = useState(null);

    const handlePickFile = async () => {
        try {
          const res = await DocumentPicker.getDocumentAsync({});
          if (res.canceled) {
            return;
          }
          if (res.assets && res.assets[0]) {
             const asset = res.assets[0];
             setFile(asset);
          } else {
            throw new Error("Document picker returned an unexpected response.");
          }
        } catch (err) {
            console.error("DocumentPicker Error:", err);
            let errorMessage = "Fehler beim Auswählen der Datei.";
            if (Platform.OS !== 'web' && err.message.includes('permission')) {
                errorMessage = "Fehler beim Auswählen der Datei. Bitte stellen Sie sicher, dass die App die Berechtigung hat, auf Ihre Dateien zuzugreifen.";
            }
            addToast(errorMessage, "error");
        }
    };

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');

        const newTransferId = uuidv4();
        setTransferId(newTransferId);
        addTransfer(newTransferId, file.name, 'upload', file.size);
        
        const data = new FormData();
        
        if (Platform.OS === 'web') {
            const response = await fetch(file.uri);
            const blob = await response.blob();
            data.append('file', new File([blob], file.name, { type: file.mimeType }));
        } else {
            data.append('file', {
                uri: file.uri,
                name: file.name,
                type: file.mimeType,
            });
        }

        data.append('requiredRole', requiredRole);

        if (categoryId === 'NEW') {
            data.append('newCategoryName', newCategoryName);
        } else {
            data.append('categoryId', categoryId);
        }

        try {
            const result = await apiClient.uploadWithProgress('/admin/files', data, newTransferId);
            if (result.success) {
                addToast('Datei erfolgreich hochgeladen!', 'success');
                updateTransfer(newTransferId, { status: 'completed' });
                onSuccess();
                onClose(); // Close on success
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            setError(err.message || 'Upload fehlgeschlagen.');
            addToast(`Upload fehlgeschlagen: ${err.message}`, 'error');
            updateTransfer(newTransferId, { status: 'error' });
        } finally {
            setIsSubmitting(false);
        }
    };

    const isSubmitDisabled = !file || (categoryId === '' || (categoryId === 'NEW' && !newCategoryName.trim()));

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
                    <Text style={styles.label}>2. Datei auswählen (Max: {MAX_FILE_SIZE_BYTES / 1024 / 1024} MB)</Text>
                    <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={handlePickFile}>
                        <Icon name="file" size={16} />
                        <Text>Datei auswählen</Text>
                    </TouchableOpacity>
                    {file && (
                        <Text style={[{marginTop: 8}]}>
                            Ausgewählt: {file.name} ({formatFileSize(file.size)})
                        </Text>
                    )}
                </View>

                <View style={styles.formGroup}>
                    <Text style={styles.label}>3. Sichtbarkeit festlegen</Text>
                     <Picker selectedValue={requiredRole} onValueChange={setRequiredRole}>
                        <Picker.Item label="Alle Benutzer" value="NUTZER" />
                        <Picker.Item label="Nur Admins" value="ADMIN" />
                    </Picker>
                </View>

                <TransferButton
                    transferId={transferId}
                    onPress={handleSubmit}
                    buttonStyle={[styles.button, styles.primaryButton, isSubmitDisabled && styles.disabledButton]}
                    textStyle={styles.buttonText}
                    defaultText="Hochladen"
                    defaultIcon="upload"
                />
            </ScrollView>
        </Modal>
    );
};

export default UploadFileModal;