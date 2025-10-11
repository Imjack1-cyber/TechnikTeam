import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, Image, ActivityIndicator, Platform, TextInput } from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../ui/Modal';
import Lightbox from '../ui/Lightbox';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing, borders } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useTransferStore } from '../../store/transferStore';
import { v4 as uuidv4 } from 'uuid';

// PhotoUploadModal would be a new component, similar to others, using a file picker
const PhotoUploadModal = ({ isOpen, onClose, onSuccess, eventId }) => {
    const { addToast } = useToast();
    const { addTransfer, updateTransfer } = useTransferStore();
    const [file, setFile] = useState(null);
    const [caption, setCaption] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handlePickFile = async () => {
        const result = await DocumentPicker.getDocumentAsync({ type: 'image/*' });
        if (!result.canceled && result.assets[0]) {
            setFile(result.assets[0]);
        }
    };

    const handleUpload = async () => {
        if (!file) {
            addToast('Bitte wählen Sie ein Foto aus.', 'error');
            return;
        }
        setIsSubmitting(true);
        const transferId = uuidv4();
        addTransfer(transferId, file.name, 'upload', file.size);
        onClose();

        const data = new FormData();
        data.append('caption', caption);
        
        if (Platform.OS === 'web') {
            const response = await fetch(file.uri);
            const blob = await response.blob();
            data.append('file', new File([blob], file.name, { type: file.mimeType }));
        } else {
            data.append('file', { uri: file.uri, name: file.name, type: file.mimeType });
        }

        try {
            const result = await apiClient.uploadWithProgress(`/public/events/${eventId}/gallery`, data, transferId);
            if (result.success) {
                addToast('Foto erfolgreich hochgeladen.', 'success');
                updateTransfer(transferId, { status: 'completed' });
                onSuccess();
            } else {
                throw new Error(result.message);
            }
        } catch(e) {
            addToast(`Upload fehlgeschlagen: ${e.message}`, 'error');
            updateTransfer(transferId, { status: 'error' });
        } finally {
            setIsSubmitting(false);
        }
    };
    
    const styles = getCommonStyles();
    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Neues Foto hochladen">
            <View>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={handlePickFile}>
                    <Text>Foto auswählen</Text>
                </TouchableOpacity>
                {file && <Text>Ausgewählt: {file.name}</Text>}
                <TextInput style={[styles.input, {marginTop: 16}]} placeholder="Bildunterschrift (optional)" value={caption} onChangeText={setCaption} />
                <TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={handleUpload} disabled={isSubmitting}>
                    {isSubmitting ? <ActivityIndicator color="#fff"/> : <Text style={styles.buttonText}>Hochladen</Text>}
                </TouchableOpacity>
            </View>
        </Modal>
    );
}

const EventGalleryTab = ({ event, user }) => {
	const apiCall = useCallback(() => apiClient.get(`/public/events/${event.id}/gallery`), [event.id]);
	const { data: photos, loading, error, reload } = useApi(apiCall);
	const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
	const [lightboxSrc, setLightboxSrc] = useState('');
	const { addToast } = useToast();

    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const isParticipant = event.assignedAttendees?.some(attendee => attendee.id === user.id);

	const getImagePath = (path) => `${apiClient.getRootUrl()}/api/v1/public/files/images/${path.split('/').pop()}`;

    const renderItem = ({ item }) => {
        const canDelete = user.isAdmin || user.id === event.leaderUserId || user.id === item.uploaderUserId;
        const imageUrl = getImagePath(item.filepath);
        return (
            <TouchableOpacity style={styles.photoCard} onPress={() => setLightboxSrc(imageUrl)}>
                <Image source={{ uri: imageUrl }} style={styles.photoImage} />
                <View style={styles.captionContainer}>
                    <Text style={styles.captionText}>{item.caption}</Text>
                    <Text style={styles.captionUser}>Von: {item.uploaderUsername}</Text>
                </View>
                {canDelete && (
                    <TouchableOpacity style={styles.deleteButton}>
                        <Icon name="times" size={14} color={colors.white} />
                    </TouchableOpacity>
                )}
            </TouchableOpacity>
        );
    };

	return (
		<View>
			{isParticipant && (
				<TouchableOpacity style={[styles.button, styles.successButton, { alignSelf: 'flex-end', margin: spacing.md }]} onPress={() => setIsUploadModalOpen(true)}>
                    <Icon name="upload" size={16} color="#fff" />
					<Text style={styles.buttonText}> Foto hochladen</Text>
				</TouchableOpacity>
			)}

			{loading && <ActivityIndicator />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={photos}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                numColumns={2}
                ListEmptyComponent={<Text style={{padding: spacing.md}}>Für diese Veranstaltung wurden noch keine Fotos hochgeladen.</Text>}
                contentContainerStyle={{padding: spacing.md}}
            />

			{isUploadModalOpen && <PhotoUploadModal isOpen={isUploadModalOpen} onClose={() => setIsUploadModalOpen(false)} onSuccess={() => { setIsUploadModalOpen(false); reload(); }} eventId={event.id} />}
			{!!lightboxSrc && <Lightbox src={lightboxSrc} onClose={() => setLightboxSrc('')} />}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        photoCard: { flex: 1, margin: spacing.xs, borderRadius: borders.radius, overflow: 'hidden', borderWidth: 1, borderColor: colors.border },
        photoImage: { width: '100%', height: 150 },
        captionContainer: { position: 'absolute', bottom: 0, left: 0, right: 0, backgroundColor: 'rgba(0,0,0,0.6)', padding: spacing.sm },
        captionText: { color: colors.white, fontSize: 14 },
        captionUser: { color: colors.white, fontSize: 12, opacity: 0.8 },
        deleteButton: { position: 'absolute', top: 5, right: 5, backgroundColor: 'rgba(0,0,0,0.5)', borderRadius: 12, width: 24, height: 24, justifyContent: 'center', alignItems: 'center' },
    });
};

export default EventGalleryTab;