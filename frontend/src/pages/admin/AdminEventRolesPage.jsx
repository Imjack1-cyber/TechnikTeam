import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, TextInput, ActivityIndicator, ScrollView, Alert } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';

const RoleModal = ({ isOpen, onClose, onSuccess, role }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const { addToast } = useToast();
    const [formData, setFormData] = useState({ name: '', description: '', iconClass: '' });

    useEffect(() => {
        if (role) {
            setFormData({ name: role.name, description: role.description || '', iconClass: role.iconClass || '' });
        } else {
            setFormData({ name: '', description: '', iconClass: 'fa-user-tag' });
        }
    }, [role]);

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');
        try {
            const result = role
                ? await apiClient.put(`/admin/event-roles/${role.id}`, formData)
                : await apiClient.post('/admin/event-roles', formData);

            if (result.success) {
                addToast(`Rolle erfolgreich ${role ? 'aktualisiert' : 'erstellt'}.`, 'success');
                onSuccess();
            } else { throw new Error(result.message); }
        } catch (err) {
            setError(err.message || 'Speichern fehlgeschlagen');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={role ? 'Rolle bearbeiten' : 'Neue Rolle erstellen'}>
            <ScrollView>
                {error && <Text style={styles.errorText}>{error}</Text>}
                <Text style={styles.label}>Rollenname</Text>
                <TextInput style={styles.input} value={formData.name} onChangeText={val => setFormData({ ...formData, name: val })} />
                <Text style={styles.label}>Beschreibung</Text>
                <TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => setFormData({ ...formData, description: val })} multiline />
                <Text style={styles.label}>Font Awesome Icon</Text>
                <TextInput style={styles.input} value={formData.iconClass} onChangeText={val => setFormData({ ...formData, iconClass: val })} placeholder="z.B. fa-user-tag" />
                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit} disabled={isSubmitting}>
                    {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Speichern</Text>}
                </TouchableOpacity>
            </ScrollView>
        </Modal>
    );
};

const AdminEventRolesPage = () => {
    const apiCall = useCallback(() => apiClient.get('/admin/event-roles'), []);
    const { data: roles, loading, error, reload } = useApi(apiCall);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingRole, setEditingRole] = useState(null);
    const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

    const openModal = (role = null) => {
        setEditingRole(role);
        setIsModalOpen(true);
    };

    const handleDelete = (role) => {
        Alert.alert(`Rolle "${role.name}" löschen?`, "Dies kann nicht rückgängig gemacht werden.", [
            { text: 'Abbrechen', style: 'cancel' },
            { text: 'Löschen', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.delete(`/admin/event-roles/${role.id}`);
                    if (result.success) {
                        addToast('Rolle gelöscht', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
            }},
        ]);
    };

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <View style={{flexDirection: 'row', alignItems: 'center', gap: 16}}>
                <Icon name={item.iconClass.replace('fa-', '')} solid size={24} />
                <Text style={styles.cardTitle}>{item.name}</Text>
            </View>
            <Text style={styles.bodyText}>{item.description}</Text>
            <View style={{flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16}}>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal(item)}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => handleDelete(item)}><Text style={styles.buttonText}>Löschen</Text></TouchableOpacity>
            </View>
        </View>
    );

    return (
        <View style={styles.container}>
            <TouchableOpacity style={[styles.button, styles.successButton, { margin: 16 }]} onPress={() => openModal()}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neue Rolle</Text>
            </TouchableOpacity>

            {loading && <ActivityIndicator size="large" />}
            {error && <Text style={styles.errorText}>{error}</Text>}

            <FlatList
                data={roles}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{ paddingHorizontal: 16 }}
            />

            <RoleModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={() => { setIsModalOpen(false); reload(); }} role={editingRole} />
        </View>
    );
};

export default AdminEventRolesPage;