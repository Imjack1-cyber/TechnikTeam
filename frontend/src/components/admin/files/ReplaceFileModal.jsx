import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView, Platform } from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import AdminModal from '../../../components/ui/AdminModal';
import apiClient, { MAX_FILE_SIZE_BYTES } from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useTransferStore } from '../../../store/transferStore';
import { v4 as uuidv4 } from 'uuid';
import TransferButton from '../../../components/ui/TransferButton';

const ReplaceFileModal = ({ isOpen, onClose, onSuccess, file, formatFileSize }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const { addToast } = useToast();
    const { addTransfer, updateTransfer } = useTransferStore();

    const [newFile, setNewFile] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [transferId, setTransferId] = useState(null);

    const handlePickFile = async () => {
        try {
            const res = await DocumentPicker.getDocumentAsync({});
            if (res.canceled) return;
            if (res.assets && res.assets[0]) {
                setNewFile(res.assets[0]);
            } else {
                throw new Error("Document picker returned an unexpected response.");
            }
        } catch (err) {
            addToast("Fehler beim Auswählen der Datei.", "error");
        }
    };

    const handleSubmit = async () => {
        if (!newFile) {
            addToast('Bitte wählen Sie eine gültige Datei aus.', 'error');
            return;
        }
        setIsSubmitting(true);
        const newTransferId = uuidv4();
        setTransferId(newTransferId);
        addTransfer(newTransferId, newFile.name, 'upload', newFile.size);

        const data = new FormData();
        
        if (Platform.OS === 'web') {
            const response = await fetch(newFile.uri);
            const blob = await response.blob();
            data.append('file', new File([blob], newFile.name, { type: newFile.mimeType }));
        } else {
            data.append('file', {
                uri: newFile.uri,
                name: newFile.name,
                type: newFile.mimeType,
            });
        }
        
        data.append('requiredRole', file.requiredRole);
        data.append('categoryId', file.categoryId || '');

        try {
            const result = await apiClient.uploadWithProgress(`/admin/files/replace/${file.id}`, data, newTransferId);
            if (result.success) {
                addToast('Datei erfolgreich ersetzt.', 'success');
                updateTransfer(newTransferId, { status: 'completed' });
                onSuccess();
                onClose();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
            updateTransfer(newTransferId, { status: 'error' });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title="Datei ersetzen"
        >
            <Text style={styles.bodyText}>Ersetze "{file?.filename}" durch eine neue Version.</Text>
             <Text style={styles.label}>Maximalgröße: {MAX_FILE_SIZE_BYTES / 1024 / 1024} MB</Text>
            <TouchableOpacity style={[styles.button, styles.secondaryButton, { alignSelf: 'flex-start', marginVertical: 16 }]} onPress={handlePickFile}>
                <Icon name="file" size={16} />
                <Text>Neue Datei auswählen</Text>
            </TouchableOpacity>
            {newFile && (
                <Text style={[{marginTop: 8}]}>
                    Ausgewählt: {newFile.name} ({formatFileSize(newFile.size)})
                </Text>
            )}
            <TransferButton
                transferId={transferId}
                onPress={handleSubmit}
                buttonStyle={[styles.button, styles.primaryButton, !newFile && styles.disabledButton]}
                textStyle={styles.buttonText}
                defaultText="Speichern"
            />
        </AdminModal>
    );
};

export default ReplaceFileModal;