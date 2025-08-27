import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, TextInput } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../ui/Modal';
import Icon from 'react-native-vector-icons/FontAwesome5';

const RenameSessionModal = ({ isOpen, onClose, onSuccess, session }) => {
    const [deviceName, setDeviceName] = useState(session.deviceName || '');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const { addToast } = useToast();

    const handleSubmit = async () => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.post(`/public/sessions/${session.id}/name`, { deviceName });
            if (result.success) {
                addToast('Gerät erfolgreich umbenannt.', 'success');
                onSuccess();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Gerät umbenennen">
            <View>
                <Text style={styles.label}>Name für diese Sitzung/dieses Gerät</Text>
                <TextInput style={styles.input} value={deviceName} onChangeText={setDeviceName} autoFocus />
                <TouchableOpacity style={styles.modalButton} onPress={handleSubmit} disabled={isSubmitting}>
                    <Text style={styles.buttonText}>{isSubmitting ? 'Speichern...' : 'Speichern'}</Text>
                </TouchableOpacity>
            </View>
        </Modal>
    );
};

const ProfileActiveSessions = () => {
    const apiCall = useCallback(() => apiClient.get('/public/sessions'), []);
    const { data: sessions, loading, error, reload } = useApi(apiCall);
    const { addToast } = useToast();
    const [renamingSession, setRenamingSession] = useState(null);

    const getDeviceIcon = (deviceType) => {
        switch (deviceType?.toLowerCase()) {
            case 'desktop': return 'desktop';
            case 'mobile': return 'mobile-alt';
            case 'tablet': return 'tablet-alt';
            default: return 'question-circle';
        }
    };

    const handleRevoke = async (session) => {
        try {
            const result = await apiClient.post(`/public/sessions/${session.jti}/revoke`);
            if (result.success) {
                addToast('Sitzung wurde beendet.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
    };
    
    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <View style={styles.cardHeader}>
                <Icon name={getDeviceIcon(item.deviceType)} size={20} style={styles.deviceIcon} />
                <Text style={styles.deviceName}>{item.deviceName || 'Unbenanntes Gerät'}</Text>
            </View>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Standort:</Text>
                <Text style={styles.value}>{item.countryCode} ({item.ipAddress})</Text>
            </View>
             <View style={styles.detailRow}>
                <Text style={styles.label}>Letzte Aktivität:</Text>
                <Text style={styles.value}>{new Date(item.timestamp).toLocaleString('de-DE')}</Text>
            </View>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => setRenamingSession(item)}>
                    <Text style={styles.secondaryButtonText}>Umbenennen</Text>
                </TouchableOpacity>
                 <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => handleRevoke(item)}>
                    <Text style={styles.dangerOutlineButtonText}>Abmelden</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

    return (
        <>
            <View style={styles.container}>
                <Text style={styles.title}>Aktive Sitzungen</Text>
                <Text style={styles.description}>Hier sehen Sie alle Geräte, auf denen Sie aktuell angemeldet sind.</Text>
                {loading && <ActivityIndicator />}
                {error && <Text style={styles.errorText}>{error}</Text>}
                <FlatList
                    data={sessions}
                    renderItem={renderItem}
                    keyExtractor={item => item.id.toString()}
                    ListEmptyComponent={<Text>Keine aktiven Sitzungen gefunden.</Text>}
                />
            </View>
            {renamingSession && (
                <RenameSessionModal isOpen={!!renamingSession} onClose={() => setRenamingSession(null)} onSuccess={() => { setRenamingSession(null); reload(); }} session={renamingSession} />
            )}
        </>
    );
};

const styles = StyleSheet.create({
    container: { marginHorizontal: 16, marginTop: 16 },
    title: { fontSize: 20, fontWeight: '600', color: '#002B5B', marginBottom: 8 },
    description: { color: '#6c757d', marginBottom: 16 },
    card: { backgroundColor: '#fff', borderRadius: 8, padding: 16, marginBottom: 12, borderWidth: 1, borderColor: '#dee2e6' },
    cardHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 8 },
    deviceIcon: { marginRight: 8, color: '#6c757d' },
    deviceName: { fontSize: 16, fontWeight: 'bold' },
    detailRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 4 },
    label: { color: '#6c757d' },
    value: { color: '#212529' },
    cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 12 },
    button: { paddingVertical: 8, paddingHorizontal: 12, borderRadius: 6 },
    secondaryButton: { borderWidth: 1, borderColor: '#6c757d' },
    secondaryButtonText: { color: '#6c757d' },
    dangerOutlineButton: { borderWidth: 1, borderColor: '#dc3545' },
    dangerOutlineButtonText: { color: '#dc3545' },
    errorText: { color: 'red' },
    // Modal Styles
    input: { width: '100%', height: 48, borderWidth: 1, borderColor: '#dee2e6', borderRadius: 6, paddingHorizontal: 12, marginVertical: 16 },
    modalButton: { backgroundColor: '#007bff', paddingVertical: 12, borderRadius: 6, alignItems: 'center' },
    buttonText: { color: '#fff', fontWeight: '500' }
});

export default ProfileActiveSessions;