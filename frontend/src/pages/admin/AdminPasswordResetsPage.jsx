import React, { useState, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import PasswordDisplayModal from '../../components/admin/users/PasswordDisplayModal';
import Clipboard from '@react-native-clipboard/clipboard';

const AdminPasswordResetsPage = () => {
    const apiCall = useCallback(() => apiClient.get('/identity-verification/pending-resets'), []);
    const { data: requests, loading, error, reload } = useApi(apiCall, { subscribeTo: 'IDENTITY_VERIFICATION' });
    const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);

    const [resettingUserRequest, setResettingUserRequest] = useState(null);
    const [newPasswordInfo, setNewPasswordInfo] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleReset = (request) => {
        setResettingUserRequest(request);
    };

    const performResetPassword = async () => {
        if (!resettingUserRequest) return;
        setIsSubmitting(true);
        try {
            const result = await apiClient.post(`/users/${resettingUserRequest.userId}/reset-password`);
            if (result.success) {
                Clipboard.setString(result.data.newPassword);
                setNewPasswordInfo({ 
                    username: result.data.username, 
                    newPassword: result.data.newPassword,
                    requestToken: resettingUserRequest.challengeToken
                });
                addToast('Passwort zurückgesetzt & in Zwischenablage kopiert.', 'success');
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
            setResettingUserRequest(null);
        }
    };

    const handlePasswordModalClose = async () => {
        const originalRequestToken = newPasswordInfo?.requestToken;
        setNewPasswordInfo(null);
        if (originalRequestToken) {
            try {
                await apiClient.post(`/identity-verification/${originalRequestToken}/complete`);
                // Reload is now handled by SSE
            } catch (err) {
                addToast('Fehler beim Abschließen der Anfrage.', 'error');
            }
        }
    };

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>Anfrage von: {item.username}</Text>
            <View style={styles.detailsListRow}>
                <Text style={styles.detailsListLabel}>Angefordert am:</Text>
                <Text style={styles.detailsListValue}>{new Date(item.createdAt).toLocaleString('de-DE')}</Text>
            </View>
            <TouchableOpacity 
                style={[styles.button, {backgroundColor: colors.warning, marginTop: spacing.md, alignSelf: 'flex-start'}]}
                onPress={() => handleReset(item)}
            >
                <Icon name="key" size={14} color={colors.textOnWarning} />
                <Text style={{color: colors.textOnWarning, fontWeight: '500'}}> Passwort manuell zurücksetzen</Text>
            </TouchableOpacity>
        </View>
    );

    return (
        <>
            <View style={styles.container}>
                <View style={{ padding: spacing.md }}>
                    <Text style={styles.title}>Passwort-Anfragen</Text>
                    <Text style={styles.subtitle}>Offene Anfragen von Benutzern, die ihr Passwort vergessen haben.</Text>
                </View>

                {loading && <ActivityIndicator size="large" />}
                {error && <Text style={styles.errorText}>{error}</Text>}

                <FlatList
                    data={requests}
                    renderItem={renderItem}
                    keyExtractor={item => item.id.toString()}
                    contentContainerStyle={{ paddingHorizontal: spacing.md }}
                    ListEmptyComponent={
                        <View style={styles.card}>
                            <Text style={styles.bodyText}>Keine offenen Anfragen vorhanden.</Text>
                        </View>
                    }
                />
            </View>
            
            {resettingUserRequest && (
                <ConfirmationModal
                    isOpen={!!resettingUserRequest}
                    onClose={() => setResettingUserRequest(null)}
                    onConfirm={performResetPassword}
                    isSubmitting={isSubmitting}
                    title={`Passwort für ${resettingUserRequest.username} zurücksetzen?`}
                    message="Dies generiert ein neues, temporäres Passwort. Der Benutzer kann sich dann damit anmelden und sein Passwort ändern."
                    confirmText="Ja, zurücksetzen"
                    confirmButtonVariant="danger"
                />
            )}
            
            {newPasswordInfo && (
                <PasswordDisplayModal
                    isOpen={!!newPasswordInfo}
                    onClose={handlePasswordModalClose}
                    username={newPasswordInfo.username}
                    newPassword={newPasswordInfo.newPassword}
                />
            )}
        </>
    );
};

export default AdminPasswordResetsPage;