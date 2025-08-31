import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, ActivityIndicator, StyleSheet } from 'react-native';
import Modal from '../../ui/Modal';
import PermissionsTab from './PermissionTab';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors } from '../../../styles/theme';
import { Picker } from '@react-native-picker/picker';

const UserModal = ({ isOpen, onClose, onSuccess, user, roles, groupedPermissions, isLoadingData }) => {
	const [activeTab, setActiveTab] = useState('general');
	const [formData, setFormData] = useState({});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

	const isEditMode = !!user;

	useEffect(() => {
        if (!isOpen) return;
		const fetchUserData = async () => {
			if (isEditMode) {
				try {
					const result = await apiClient.get(`/users/${user.id}`);
					if (result.success) {
						setFormData({
							username: result.data.username || '',
							roleId: result.data.roleId || '',
							classYear: result.data.classYear?.toString() || '',
							className: result.data.className || '',
							email: result.data.email || '',
							adminNotes: result.data.adminNotes || '',
							permissionIds: new Set(result.data.permissions.map(p => p.id))
						});
					}
				} catch (err) { setError('Benutzerdetails konnten nicht geladen werden.'); }
			} else {
				setFormData({
					username: '', password: '',
					roleId: roles.find(r => r.roleName === 'NUTZER')?.id || '',
					classYear: '', className: '', email: '', adminNotes: '',
					permissionIds: new Set()
				});
			}
		};
		fetchUserData();
	}, [user, isEditMode, roles, isOpen]);

	const handleChange = (name, value) => setFormData(prev => ({ ...prev, [name]: value }));
	const handlePermissionChange = (permissionId) => setFormData(prev => {
        const newIds = new Set(prev.permissionIds);
        if (newIds.has(permissionId)) newIds.delete(permissionId); else newIds.add(permissionId);
        return { ...prev, permissionIds: newIds };
    });

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		const payload = { ...formData, permissionIds: Array.from(formData.permissionIds || []) };

		try {
			const result = isEditMode ? await apiClient.put(`/users/${user.id}`, payload) : await apiClient.post('/users', payload);
			if (result.success) {
				addToast(`Benutzer ${isEditMode ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Ein Fehler ist aufgetreten.');
		} finally {
			setIsSubmitting(false);
		}
	};
    
    const renderGeneralTab = () => (
        <View>
            <Text style={styles.label}>Benutzername</Text>
            <TextInput style={styles.input} value={formData.username || ''} onChangeText={val => handleChange('username', val)} />
            {!isEditMode && <>
                <Text style={styles.label}>Passwort</Text>
                <TextInput style={styles.input} value={formData.password || ''} onChangeText={val => handleChange('password', val)} secureTextEntry />
            </>}
            <Text style={styles.label}>Rolle</Text>
            <Picker selectedValue={formData.roleId} onValueChange={val => handleChange('roleId', val)}>
                {roles.map(role => <Picker.Item key={role.id} label={role.roleName} value={role.id} />)}
            </Picker>
            <Text style={styles.label}>E-Mail</Text>
            <TextInput style={styles.input} value={formData.email || ''} onChangeText={val => handleChange('email', val)} keyboardType="email-address"/>
        </View>
    );

    const renderNotesTab = () => (
        <View>
            <Text style={styles.label}>Interne Notizen</Text>
            <TextInput style={[styles.input, styles.textArea]} value={formData.adminNotes || ''} onChangeText={val => handleChange('adminNotes', val)} multiline/>
        </View>
    );

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={isEditMode ? `Benutzer: ${user.username}` : 'Neuen Benutzer anlegen'}>
			<ScrollView>
                <View style={styles.tabs}>
                    <TouchableOpacity style={[styles.tabButton, activeTab === 'general' && styles.activeTab]} onPress={() => setActiveTab('general')}><Text>Allgemein</Text></TouchableOpacity>
                    <TouchableOpacity style={[styles.tabButton, activeTab === 'permissions' && styles.activeTab]} onPress={() => setActiveTab('permissions')}><Text>Berechtigungen</Text></TouchableOpacity>
                    {isEditMode && <TouchableOpacity style={[styles.tabButton, activeTab === 'notes' && styles.activeTab]} onPress={() => setActiveTab('notes')}><Text>Notizen</Text></TouchableOpacity>}
                </View>

				{error && <Text style={styles.errorText}>{error}</Text>}
				
                {activeTab === 'general' && renderGeneralTab()}
                {activeTab === 'permissions' && <PermissionsTab groupedPermissions={groupedPermissions} assignedIds={formData.permissionIds || new Set()} onPermissionChange={handlePermissionChange} isLoading={isLoadingData} />}
                {activeTab === 'notes' && isEditMode && renderNotesTab()}

				<TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={handleSubmit} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Benutzer speichern</Text>}
				</TouchableOpacity>
			</ScrollView>
		</Modal>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        tabs: { flexDirection: 'row', borderBottomWidth: 1, borderColor: colors.border, marginBottom: 16 },
        tabButton: { paddingBottom: 8, marginRight: 16 },
        activeTab: { borderBottomWidth: 3, borderBottomColor: colors.primary },
    });
};

export default UserModal;