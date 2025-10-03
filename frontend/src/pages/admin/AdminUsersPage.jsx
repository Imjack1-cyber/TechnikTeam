import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, TextInput, Platform, ScrollView } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import UserModal from '../../components/admin/users/UserModal';
import useAdminData from '../../hooks/useAdminData';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import AdminModal from '../../components/ui/AdminModal';
import ScrollableContent from '../../components/ui/ScrollableContent';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import Modal from '../../components/ui/Modal';

const SuspendUserModal = ({ isOpen, onClose, user, onSuccess }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	const [duration, setDuration] = useState('7d');
	const [reason, setReason] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

    useEffect(() => {
        if(isOpen) {
            console.log(`SuspendUserModal opened for user: ${user?.username}`);
        }
    }, [isOpen, user]);

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
		<AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={`Benutzer sperren: ${user.username}`}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Benutzer sperren"
            submitButtonVariant="danger"
        >
            {error && <Text style={styles.errorText}>{error}</Text>}
            <Text style={styles.label}>Dauer (z.B. 1h, 7d, indefinite)</Text>
            <TextInput style={styles.input} value={duration} onChangeText={setDuration} />
            <Text style={styles.label}>Grund</Text>
            <TextInput style={[styles.input, styles.textArea]} value={reason} onChangeText={setReason} multiline/>
        </AdminModal>
	);
};

const PasswordDisplayModal = ({ isOpen, onClose, username, newPassword }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Neues Passwort">
            <Text style={styles.bodyText}>Das temporäre Passwort für <Text style={{fontWeight: 'bold'}}>{username}</Text> lautet:</Text>
            <TextInput
                style={[styles.input, {textAlign: 'center', fontWeight: 'bold', fontSize: 18, marginVertical: 16}]}
                value={newPassword}
                editable={false}
                selectTextOnFocus
            />
            <Text style={styles.bodyText}>Bitte geben Sie dieses Passwort sicher an den Benutzer weiter.</Text>
        </Modal>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        badgeSuccess: { backgroundColor: colors.success, color: colors.white, paddingHorizontal: 8, paddingVertical: 4, borderRadius: 12, overflow: 'hidden' },
        badgeDanger: { backgroundColor: colors.danger, color: colors.white, paddingHorizontal: 8, paddingVertical: 4, borderRadius: 12, overflow: 'hidden' },
        actionsContainer: { flexDirection: 'row', flexWrap: 'wrap', marginTop: 12 },
        actionButton: { 
            backgroundColor: colors.primaryLight, 
            paddingVertical: 8,
            paddingHorizontal: 12,
            borderRadius: 6, 
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'center',
            marginRight: 8,
            marginBottom: 8,
        },
        actionButtonText: { 
            color: colors.text,
            marginLeft: 6,
            fontWeight: '500'
        },
    });
};

const AdminUsersPage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/users'), []);
	const { data: users, loading, error, reload } = useApi(apiCall, { subscribeTo: 'USER' });
	const adminFormData = useAdminData();
	const { addToast } = useToast();
    const currentUser = useAuthStore(state => state.user);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingUser, setEditingUser] = useState(null);
    const [suspendingUser, setSuspendingUser] = useState(null);
    const [unsuspendingUser, setUnsuspendingUser] = useState(null);
    const [deletingUser, setDeletingUser] = useState(null);
    const [resettingUser, setResettingUser] = useState(null);
    const [newPasswordInfo, setNewPasswordInfo] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

	const openModal = (user = null) => { setEditingUser(user); setIsModalOpen(true); };
	const handleSuccess = () => { setIsModalOpen(false); setEditingUser(null); setSuspendingUser(null); reload(); };

    const performResetPassword = async () => {
        if (!resettingUser) return;
        setIsSubmitting(true);
        try {
            const result = await apiClient.post(`/users/${resettingUser.id}/reset-password`);
            if (result.success) {
                setNewPasswordInfo({ username: result.data.username, newPassword: result.data.newPassword });
                addToast('Passwort zurückgesetzt.', 'success');
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
        finally {
            setIsSubmitting(false);
            setResettingUser(null);
        }
    };
    
    const performUnsuspend = async () => {
        if (!unsuspendingUser) return;
        setIsSubmitting(true);
        try {
            const result = await apiClient.post(`/admin/users/${unsuspendingUser.id}/unsuspend`);
            if (result.success) {
                addToast('Benutzer erfolgreich entsperrt.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Entsperren fehlgeschlagen: ${err.message}`, 'error'); }
        finally {
            setIsSubmitting(false);
            setUnsuspendingUser(null);
        }
    };

    const performDelete = async () => {
        if (!deletingUser) return;
        setIsSubmitting(true);
        try {
            const result = await apiClient.delete(`/users/${deletingUser.id}`);
            if (result.success) {
                addToast('Benutzer erfolgreich gelöscht.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error'); }
        finally {
            setIsSubmitting(false);
            setDeletingUser(null);
        }
    };
    
    const renderItem = ({ item: user }) => {
        const isLocked = user.isLocked || user.status === 'SUSPENDED';
        const canDelete = currentUser?.id === 1 ? (user.id !== 1) : (user.roleName !== 'ADMIN');
        const canSuspend = currentUser?.id === 1 ? user.id !== 1 : user.roleName !== 'ADMIN';

        return (
            <View style={styles.card}>
                <View style={{flexDirection: 'row', justifyContent: 'space-between'}}>
                    <Text style={styles.cardTitle}>{user.username}</Text>
                    <Text style={isLocked ? styles.badgeDanger : styles.badgeSuccess}>{isLocked ? 'Gesperrt' : 'Aktiv'}</Text>
                </View>
                <Text>ID: {user.id} | Rolle: {user.roleName}</Text>
                <View style={styles.actionsContainer}>
                    <TouchableOpacity style={styles.actionButton} onPress={() => openModal(user)}>
                        <Icon name="edit" size={14} color={colors.text} />
                        <Text style={styles.actionButtonText}>Bearbeiten</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={styles.actionButton} onPress={() => navigation.navigate('UserProfile', { userId: user.id })}>
                        <Icon name="id-card" size={14} color={colors.text} />
                        <Text style={styles.actionButtonText}>Profil</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={styles.actionButton} onPress={() => setResettingUser(user)}>
                        <Icon name="key" size={14} color={colors.text} />
                        <Text style={styles.actionButtonText}>Reset PW</Text>
                    </TouchableOpacity>
                    {isLocked ? (
                        <TouchableOpacity style={[styles.actionButton, {backgroundColor: colors.success}]} onPress={() => setUnsuspendingUser(user)}>
                            <Icon name="unlock" size={14} color={colors.white} />
                            <Text style={[styles.actionButtonText, {color: colors.white}]}>Entsperren</Text>
                        </TouchableOpacity>
                    ) : (
                        <TouchableOpacity style={[styles.actionButton, {backgroundColor: colors.warning}, !canSuspend && styles.disabledButton]} onPress={() => setSuspendingUser(user)} disabled={!canSuspend}>
                            <Icon name="user-lock" size={14} color={colors.black} />
                            <Text style={[styles.actionButtonText, {color: colors.black}]}>Sperren</Text>
                        </TouchableOpacity>
                    )}
                    {canDelete && (
                         <TouchableOpacity style={[styles.actionButton, {backgroundColor: colors.danger}]} onPress={() => setDeletingUser(user)}>
                            <Icon name="trash" size={14} color="#fff" />
                            <Text style={[styles.actionButtonText, {color: '#fff'}]}>Löschen</Text>
                         </TouchableOpacity>
                    )}
                </View>
            </View>
        );
    };

	return (
		<ScrollableContent>
			<TouchableOpacity style={[styles.button, styles.successButton, {margin: 16}]} onPress={() => openModal()}>
                <Icon name="user-plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neuen Benutzer anlegen</Text>
            </TouchableOpacity>
			{(loading || adminFormData.loading) && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			{users?.map(user => renderItem({item: user}))}
			{isModalOpen && !adminFormData.loading && <UserModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={handleSuccess} user={editingUser} roles={adminFormData.roles} groupedPermissions={adminFormData.groupedPermissions} />}
            {suspendingUser && <SuspendUserModal isOpen={!!suspendingUser} onClose={() => setSuspendingUser(null)} onSuccess={handleSuccess} user={suspendingUser} />}
            {newPasswordInfo && <PasswordDisplayModal isOpen={!!newPasswordInfo} onClose={() => setNewPasswordInfo(null)} username={newPasswordInfo.username} newPassword={newPasswordInfo.newPassword} />}
            {resettingUser && (
                <ConfirmationModal
                    isOpen={!!resettingUser}
                    onClose={() => setResettingUser(null)}
                    onConfirm={performResetPassword}
                    title={`Passwort für ${resettingUser.username} zurücksetzen?`}
                    message="Ein neues, temporäres Passwort wird generiert und angezeigt. Geben Sie es sicher an den Benutzer weiter."
                    confirmText="Zurücksetzen"
                    isSubmitting={isSubmitting}
                />
            )}
            {unsuspendingUser && (
                <ConfirmationModal
                    isOpen={!!unsuspendingUser}
                    onClose={() => setUnsuspendingUser(null)}
                    onConfirm={performUnsuspend}
                    title={`Benutzer "${unsuspendingUser.username}" entsperren?`}
                    message="Die Sperre des Benutzers wird aufgehoben und alle Anmeldeversuche werden zurückgesetzt."
                    confirmText="Entsperren"
                    confirmButtonVariant="success"
                    isSubmitting={isSubmitting}
                />
            )}
            {deletingUser && (
                <ConfirmationModal
                    isOpen={!!deletingUser}
                    onClose={() => setDeletingUser(null)}
                    onConfirm={performDelete}
                    title={`Benutzer "${deletingUser.username}" löschen?`}
                    message="Diese Aktion kann nicht rückgängig gemacht werden."
                    confirmText="Löschen"
                    isSubmitting={isSubmitting}
                />
            )}
		</ScrollableContent>
	);
};

export default AdminUsersPage;