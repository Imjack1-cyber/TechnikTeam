import React, { useState, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, ScrollView } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import PermissionTab from '../../components/admin/users/PermissionTab';

const AdminPermissionsPage = () => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const { addToast } = useToast();

    const [activeTab, setActiveTab] = useState('user');
    const [selectedUser, setSelectedUser] = useState(null);
    const [selectedRole, setSelectedRole] = useState(null);

    const apiCall = useCallback(() => apiClient.get('/admin/permissions/overview'), []);
    const { data: overviewData, loading, error, reload } = useApi(apiCall);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSaveUserPermissions = async (userId, permissionIds) => {
        setIsSubmitting(true);
        try {
            const userToUpdate = overviewData.users.find(u => u.id === userId);
            const payload = {
                ...userToUpdate,
                permissionIds: Array.from(permissionIds)
            };
            const result = await apiClient.put(`/users/${userId}`, payload);
            if (result.success) {
                addToast('Benutzerberechtigungen aktualisiert.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };
    
    const handleSaveRolePermissions = async (roleId, permissionIds) => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.put(`/admin/permissions/roles/${roleId}`, Array.from(permissionIds));
            if (result.success) {
                addToast('Rollenberechtigungen aktualisiert.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    const renderUserTab = () => {
        const directPermissions = selectedUser ? new Set(overviewData.directUserPermissions[selectedUser.id]) : new Set();
        const rolePermissions = selectedUser ? new Set(overviewData.rolePermissions[selectedUser.roleId]) : new Set();
        const allPermissionsForRole = selectedRole ? new Set(overviewData.rolePermissions[selectedRole.id]) : new Set();

        return (
             <View style={styles.tabContent}>
                <View style={styles.listColumn}>
                    <FlatList
                        data={overviewData?.users || []}
                        keyExtractor={item => item.id.toString()}
                        renderItem={({ item }) => (
                            <TouchableOpacity style={[styles.listItem, selectedUser?.id === item.id && styles.activeListItem]} onPress={() => setSelectedUser(item)}>
                                <Text style={[styles.listItemText, selectedUser?.id === item.id && {color: colors.primary}]}>{item.username}</Text>
                            </TouchableOpacity>
                        )}
                    />
                </View>
                <View style={styles.detailsColumn}>
                    {selectedUser ? (
                        <>
                            <PermissionTab
                                groupedPermissions={overviewData.groupedPermissions}
                                assignedIds={directPermissions}
                                inheritedIds={rolePermissions}
                                onPermissionChange={(permId) => {
                                    const newSet = new Set(directPermissions);
                                    if (newSet.has(permId)) newSet.delete(permId);
                                    else newSet.add(permId);
                                    setSelectedUser(prev => ({...prev, directPermissions: newSet})); // Optimistic update for UI
                                    handleSaveUserPermissions(selectedUser.id, newSet);
                                }}
                            />
                        </>
                    ) : <Text style={styles.placeholderText}>Wählen Sie einen Benutzer aus.</Text>}
                </View>
            </View>
        );
    };
    
    const renderRoleTab = () => {
        const permissionsForRole = selectedRole ? new Set(overviewData.rolePermissions[selectedRole.id]) : new Set();

        return (
             <View style={styles.tabContent}>
                <View style={styles.listColumn}>
                    <FlatList
                        data={overviewData?.roles || []}
                        keyExtractor={item => item.id.toString()}
                        renderItem={({ item }) => (
                            <TouchableOpacity style={[styles.listItem, selectedRole?.id === item.id && styles.activeListItem]} onPress={() => setSelectedRole(item)}>
                                <Text style={[styles.listItemText, selectedRole?.id === item.id && {color: colors.primary}]}>{item.roleName}</Text>
                            </TouchableOpacity>
                        )}
                    />
                </View>
                <View style={styles.detailsColumn}>
                    {selectedRole ? (
                        <>
                            <PermissionTab
                                groupedPermissions={overviewData.groupedPermissions}
                                assignedIds={permissionsForRole}
                                inheritedIds={new Set()} // Roles don't inherit
                                onPermissionChange={(permId) => {
                                    const newSet = new Set(permissionsForRole);
                                    if (newSet.has(permId)) newSet.delete(permId);
                                    else newSet.add(permId);
                                    handleSaveRolePermissions(selectedRole.id, newSet);
                                }}
                            />
                        </>
                    ) : <Text style={styles.placeholderText}>Wählen Sie eine Rolle aus.</Text>}
                </View>
            </View>
        );
    };

    return (
        <View style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="key" size={24} style={styles.headerIcon} />
                <Text style={styles.title}>Rollen & Berechtigungen</Text>
            </View>
            <View style={styles.tabContainer}>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'user' && styles.activeTab]} onPress={() => setActiveTab('user')}>
                    <Text style={[styles.tabText, activeTab === 'user' && styles.activeTabText]}>Nach Benutzer</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'role' && styles.activeTab]} onPress={() => setActiveTab('role')}>
                    <Text style={[styles.tabText, activeTab === 'role' && styles.activeTabText]}>Nach Rolle</Text>
                </TouchableOpacity>
            </View>
            {loading && <ActivityIndicator size="large" />}
            {error && <Text style={styles.errorText}>{error}</Text>}
            {overviewData && (activeTab === 'user' ? renderUserTab() : renderRoleTab())}
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1, backgroundColor: colors.background },
        headerContainer: { padding: spacing.md, flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: spacing.sm },
        tabContainer: { flexDirection: 'row', paddingHorizontal: spacing.md, borderBottomWidth: 1, borderColor: colors.border },
        tabButton: { paddingVertical: spacing.md, paddingHorizontal: spacing.sm, marginRight: spacing.md, borderBottomWidth: 3, borderBottomColor: 'transparent' },
        activeTab: { borderBottomColor: colors.primary },
        tabText: { color: colors.textMuted, fontWeight: '500' },
        activeTabText: { color: colors.primary },
        tabContent: { flexDirection: 'row', flex: 1 },
        listColumn: { width: '30%', borderRightWidth: 1, borderColor: colors.border },
        listItem: { padding: spacing.md, borderBottomWidth: 1, borderColor: colors.border },
        activeListItem: { backgroundColor: colors.primaryLight },
        listItemText: { fontSize: typography.body, color: colors.text },
        detailsColumn: { flex: 1, padding: spacing.md },
        placeholderText: { color: colors.textMuted, fontStyle: 'italic', textAlign: 'center', marginTop: spacing.lg }
    });
};

export default AdminPermissionsPage;