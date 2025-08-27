import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, TextInput } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import UserModal from '../../components/admin/users/UserModal';
import useAdminData from '../../hooks/useAdminData';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const SuspendUserModal = ({ isOpen, onClose, user, onSuccess }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	const [duration, setDuration] = useState('7d');
	const [reason, setReason] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		try {
			const result = await apiClient.post(`/admin/users/${user.id}/suspend`, { duration, reason });
			if (result.success) {
				addToast(`Benutzer ${user.username} wurde gesperrt.`, 'success');
				onSuccess();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Sperren fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`Benutzer sperren: ${user.username}`}>
			<View>
				{error && <Text style={styles.errorText}>{error}</Text>}
				<Text style={styles.label}>Dauer (z.B. 1h, 7d, indefinite)</Text>
				<TextInput style={styles.input} value={duration} onChangeText={setDuration} />
				<Text style={styles.label}>Grund</Text>
				<TextInput style={[styles.input, styles.textArea]} value={reason} onChangeText={setReason} multiline/>
				<TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={handleSubmit} disabled={isSubmitting}>
                    {isSubmitting ? <ActivityIndicator color="#fff"/> : <Text style={styles.buttonText}>Benutzer sperren</Text>}
				</TouchableOpacity>
			</View>
		</Modal>
	);
};

const AdminUsersPage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/users'), []);
	const { data: users, loading, error, reload } = useApi(apiCall);
	const adminFormData = useAdminData();
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingUser, setEditingUser] = useState(null);
    const [suspendingUser, setSuspendingUser] = useState(null);

	const openModal = (user = null) => { setEditingUser(user); setIsModalOpen(true); };
	const handleSuccess = () => { setIsModalOpen(false); setEditingUser(null); setSuspendingUser(null); reload(); };

	const handleResetPassword = (user) => {
        Alert.alert(`Passwort für ${user.username} zurücksetzen?`, "Ein neues, temporäres Passwort wird generiert.", [
            { text: 'Abbrechen', style: 'cancel' },
            { text: 'Zurücksetzen', onPress: async () => {
                try {
                    const result = await apiClient.post(`/users/${user.id}/reset-password`);
                    if (result.success) {
                        Alert.alert('Passwort zurückgesetzt', `Das neue Passwort für ${result.data.username} ist: ${result.data.newPassword}\n\nBitte geben Sie es sicher weiter.`, [{text: 'OK'}]);
                        addToast('Passwort zurückgesetzt.', 'success');
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
            }},
        ]);
	};
    
    const handleUnsuspend = (user) => {
        Alert.alert(`Benutzer "${user.username}" entsperren?`, "", [
            { text: 'Abbrechen', style: 'cancel' },
            { text: 'Entsperren', onPress: async () => {
                try {
                    const result = await apiClient.post(`/admin/users/${user.id}/unsuspend`);
                    if (result.success) {
                        addToast('Benutzer erfolgreich entsperrt.', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Entsperren fehlgeschlagen: ${err.message}`, 'error'); }
            }},
        ]);
    }
    
    const renderItem = ({ item: user }) => {
        const isLocked = user.isLocked || user.status === 'SUSPENDED';
        return (
            <View style={styles.card}>
                <View style={{flexDirection: 'row', justifyContent: 'space-between'}}>
                    <Text style={styles.cardTitle}>{user.username}</Text>
                    <Text style={isLocked ? styles.badgeDanger : styles.badgeSuccess}>{isLocked ? 'Gesperrt' : 'Aktiv'}</Text>
                </View>
                <Text>ID: {user.id} | Rolle: {user.roleName}</Text>
                <View style={styles.actionsContainer}>
                    <TouchableOpacity style={styles.actionButton} onPress={() => openModal(user)}><Text>Bearbeiten</Text></TouchableOpacity>
                    <TouchableOpacity style={styles.actionButton} onPress={() => navigation.navigate('UserProfile', { userId: user.id })}><Text>Profil</Text></TouchableOpacity>
                    <TouchableOpacity style={styles.actionButton} onPress={() => handleResetPassword(user)}><Text>Reset PW</Text></TouchableOpacity>
                    {isLocked ? (
                        <TouchableOpacity style={[styles.actionButton, {backgroundColor: colors.success}]} onPress={() => handleUnsuspend(user)}><Text style={{color: colors.white}}>Entsperren</Text></TouchableOpacity>
                    ) : (
                        <TouchableOpacity style={[styles.actionButton, {backgroundColor: colors.warning}]} onPress={() => setSuspendingUser(user)} disabled={user.roleName === 'ADMIN'}><Text style={{color: colors.black}}>Sperren</Text></TouchableOpacity>
                    )}
                </View>
            </View>
        );
    };

	return (
		<View style={styles.container}>
			<TouchableOpacity style={[styles.button, styles.successButton, {margin: 16}]} onPress={() => openModal()}>
                <Icon name="user-plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neuen Benutzer anlegen</Text>
            </TouchableOpacity>
			{(loading || adminFormData.loading) && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			<FlatList data={users} renderItem={renderItem} keyExtractor={item => item.id.toString()} contentContainerStyle={{paddingHorizontal: 16}} />
			{isModalOpen && !adminFormData.loading && <UserModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={handleSuccess} user={editingUser} roles={adminFormData.roles} groupedPermissions={adminFormData.groupedPermissions} />}
            {suspendingUser && <SuspendUserModal isOpen={!!suspendingUser} onClose={() => setSuspendingUser(null)} onSuccess={handleSuccess} user={suspendingUser} />}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        badgeSuccess: { backgroundColor: colors.success, color: colors.white, paddingHorizontal: 8, paddingVertical: 4, borderRadius: 12, overflow: 'hidden' },
        badgeDanger: { backgroundColor: colors.danger, color: colors.white, paddingHorizontal: 8, paddingVertical: 4, borderRadius: 12, overflow: 'hidden' },
        actionsContainer: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginTop: 12 },
        actionButton: { backgroundColor: colors.primaryLight, padding: 8, borderRadius: 6 },
    });
};

export default AdminUsersPage;