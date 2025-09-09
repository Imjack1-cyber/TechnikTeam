import React, { useState, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import useApi from '../../../hooks/useApi';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors, typography, spacing, borders } from '../../../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';
import { Picker } from '@react-native-picker/picker';

const AdminEventTeamTab = ({ event, onTeamUpdate }) => {
	const { addToast } = useToast();
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [assignedUsers, setAssignedUsers] = useState(event.assignedAttendees || []);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const rolesApiCall = useCallback(() => apiClient.get('/admin/event-roles'), []);
	const { data: allRoles } = useApi(rolesApiCall);

	const availableUsersApiCall = useCallback(() => apiClient.get(`/users?eventId=${event.id}`), [event.id]);
	const { data: availableUsers, loading: availableLoading } = useApi(availableUsersApiCall);

	const assignedUserIds = useMemo(() => new Set(assignedUsers.map(u => u.id)), [assignedUsers]);
	const unassignedAvailableUsers = useMemo(() => availableUsers?.filter(u => !assignedUserIds.has(u.id)) || [], [availableUsers, assignedUserIds]);

	const handleAssignUser = (user) => {
		setAssignedUsers(prev => [...prev, { ...user, assignedEventRoleId: null }]);
	};

	const handleUnassignUser = (userId) => {
		setAssignedUsers(prev => prev.filter(u => u.id !== userId));
	};

	const handleRoleChange = (userId, newRoleId) => {
		setAssignedUsers(prev => prev.map(u => u.id === userId ? { ...u, assignedEventRoleId: newRoleId } : u));
	};

	const handleSaveTeam = async () => {
		setIsSubmitting(true);
		const payload = assignedUsers.map(u => ({ userId: u.id, roleId: u.assignedEventRoleId || null }));
		try {
			const result = await apiClient.post(`/events/${event.id}/assignments`, payload);
			if (result.success) {
				addToast('Team erfolgreich gespeichert!', 'success');
				onTeamUpdate();
			} else { throw new Error(result.message); }
		} catch (err) {
			addToast(`Fehler beim Speichern: ${err.message}`, 'error');
		} finally {
			setIsSubmitting(false);
		}
	};
    
    const renderUnassigned = ({item}) => (
        <View style={styles.listItem}>
            <Text style={styles.userName}>{item.username}</Text>
            <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => handleAssignUser(item)}>
                <Icon name="plus" size={14} color="#fff" />
                <Text style={styles.buttonText}>Hinzufügen</Text>
            </TouchableOpacity>
        </View>
    );

     const renderAssigned = ({item}) => (
        <View style={styles.listItem}>
            <TouchableOpacity onPress={() => handleUnassignUser(item.id)}>
                <Icon name="times" size={20} color={colors.danger} />
            </TouchableOpacity>
            <Text style={styles.userName}>{item.username}</Text>
            <View style={styles.pickerContainer}>
                <Picker selectedValue={item.assignedEventRoleId} onValueChange={(val) => handleRoleChange(item.id, val)}>
                    <Picker.Item label="(Unzugewiesen)" value={null} />
                    {allRoles?.map(role => <Picker.Item key={role.id} label={role.name} value={role.id} />)}
                </Picker>
            </View>
        </View>
    );

	return (
		<View>
			<TouchableOpacity style={[styles.button, styles.successButton, { alignSelf: 'flex-end', marginBottom: 16 }]} onPress={handleSaveTeam} disabled={isSubmitting}>
				{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Team speichern</Text>}
			</TouchableOpacity>
			<View style={styles.columnsContainer}>
				<View style={styles.column}>
					<Text style={styles.columnTitle}>Verfügbare Mitglieder</Text>
                    {availableLoading ? <ActivityIndicator/> : 
                        <FlatList data={unassignedAvailableUsers} renderItem={renderUnassigned} keyExtractor={item => item.id.toString()} />
                    }
				</View>
				<View style={styles.column}>
					<Text style={styles.columnTitle}>Zugewiesenes Team</Text>
                    <FlatList data={assignedUsers} renderItem={renderAssigned} keyExtractor={item => item.id.toString()} />
				</View>
			</View>
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        columnsContainer: { flexDirection: 'row', gap: spacing.md },
        column: { flex: 1, backgroundColor: colors.background, padding: spacing.sm, borderRadius: borders.radius },
        columnTitle: { fontSize: typography.h4, fontWeight: 'bold', marginBottom: spacing.md },
        listItem: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingVertical: spacing.sm, borderBottomWidth: 1, borderColor: colors.border },
        userName: { flex: 1, fontSize: typography.body },
        pickerContainer: { flex: 1.5, borderWidth: 1, borderColor: colors.border, borderRadius: 8 },
    });
};

export default AdminEventTeamTab;