import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import AdminModal from '../../components/ui/AdminModal';

const RequestActionModal = ({ isOpen, onClose, onConfirm, request, action, isSubmitting }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    if (!request) return null;

    let changes = {};
    try {
        changes = JSON.parse(request.requestedChanges);
    } catch (e) {
        console.error("Failed to parse requested changes JSON:", e);
    }

    const title = action === 'approve' ? 'Antrag genehmigen?' : 'Antrag ablehnen?';
    const confirmText = action === 'approve' ? 'Genehmigen' : 'Ablehnen';
    const confirmButtonVariant = action === 'approve' ? 'success' : 'danger';

    const changeLabels = {
        email: 'E-Mail',
        classYear: 'Jahrgang',
        className: 'Klasse',
        profileIconClass: 'Profil-Icon'
    };

    return (
        <AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={title}
            onSubmit={onConfirm}
            isSubmitting={isSubmitting}
            submitText={confirmText}
            submitButtonVariant={confirmButtonVariant}
        >
            <Text style={styles.bodyText}>
                Sind Sie sicher, dass Sie den folgenden Antrag von <Text style={{ fontWeight: 'bold' }}>{request.username}</Text> {action === 'approve' ? 'genehmigen' : 'ablehnen'} möchten?
            </Text>
            <View style={styles.changesContainer}>
                {Object.entries(changes).map(([key, value]) => (
                    <View key={key} style={styles.changeRow}>
                        <Text style={styles.changeKey}>{changeLabels[key] || key}:</Text>
                        <Text style={styles.changeValue}>{String(value)}</Text>
                    </View>
                ))}
            </View>
        </AdminModal>
    );
};


const AdminRequestsPage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/requests/pending'), []);
	const { data: requests, loading, error, reload } = useApi(apiCall);
	const [actionableRequest, setActionableRequest] = useState(null); // { request: {...}, action: 'approve' | 'deny' }
    const [isSubmitting, setIsSubmitting] = useState(false);
	const { addToast } = useToast();

    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

	const handleConfirmAction = async () => {
        if (!actionableRequest) return;
        
        const { request, action } = actionableRequest;
        setIsSubmitting(true);
		try {
			const result = await apiClient.post(`/requests/${request.id}/${action}`);
			if (result.success) {
				addToast(`Antrag #${request.id} erfolgreich ${action === 'approve' ? 'genehmigt' : 'abgelehnt'}.`, 'success');
				reload();
			} else { throw new Error(result.message); }
		} catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
        finally {
            setIsSubmitting(false);
            setActionableRequest(null);
        }
	};

    const renderItem = ({ item }) => {
        let changes = {};
        try {
            changes = JSON.parse(item.requestedChanges);
        } catch (e) {}

        const changeLabels = {
            email: 'E-Mail',
            classYear: 'Jahrgang',
            className: 'Klasse',
            profileIconClass: 'Profil-Icon'
        };

        return (
            <View style={styles.card}>
                <Text style={styles.cardTitle}>Antrag von {item.username}</Text>
                <View style={styles.detailRow}>
                    <Text style={styles.label}>Datum:</Text>
                    <Text style={styles.value}>{new Date(item.requestedAt).toLocaleString('de-DE')}</Text>
                </View>

                <View style={styles.changesContainer}>
                    <Text style={styles.label}>Beantragte Änderungen:</Text>
                     {Object.entries(changes).map(([key, value]) => (
                        <View key={key} style={styles.changeRow}>
                            <Text style={styles.changeKey}>{changeLabels[key] || key}:</Text>
                            <Text style={styles.changeValue}>{String(value)}</Text>
                        </View>
                     ))}
                </View>

                <View style={styles.cardActions}>
                    <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => setActionableRequest({ request: item, action: 'approve' })}>
                        <Text style={styles.buttonText}>Genehmigen</Text>
                    </TouchableOpacity>
                     <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => setActionableRequest({ request: item, action: 'deny' })}>
                        <Text style={styles.buttonText}>Ablehnen</Text>
                    </TouchableOpacity>
                </View>
            </View>
        );
    };

	return (
        <View style={styles.container}>
            {loading ? (
                <View style={styles.centered}><ActivityIndicator size="large" /></View>
            ) : error ? (
                <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>
            ) : (
                <FlatList
                    data={requests}
                    renderItem={renderItem}
                    keyExtractor={item => item.id.toString()}
                    contentContainerStyle={styles.contentContainer}
                    ListHeaderComponent={
                        <Text style={styles.subtitle}>
                            Hier sehen Sie alle offenen Anträge auf Profiländerungen von Benutzern.
                        </Text>
                    }
                    ListEmptyComponent={
                        <View style={[styles.card, { marginTop: 16 }]}>
                            <Text>Keine offenen Anträge vorhanden.</Text>
                        </View>
                    }
                />
            )}
            
            <RequestActionModal
                isOpen={!!actionableRequest}
                onClose={() => setActionableRequest(null)}
                onConfirm={handleConfirmAction}
                request={actionableRequest?.request}
                action={actionableRequest?.action}
                isSubmitting={isSubmitting}
            />
        </View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        cardTitle: {
            fontSize: typography.h4,
            fontWeight: 'bold',
            color: colors.heading,
            marginBottom: spacing.sm,
            paddingBottom: 0,
            borderBottomWidth: 0,
        },
        cardActions: { 
            flexDirection: 'row', 
            justifyContent: 'flex-end', 
            gap: 8, 
            marginTop: 16 
        },
        changesContainer: {
            marginTop: spacing.md,
            paddingTop: spacing.sm,
            borderTopWidth: 1,
            borderColor: colors.border,
        },
        changeRow: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            paddingVertical: 4,
        },
        changeKey: {
            color: colors.textMuted,
            fontWeight: '500',
        },
        changeValue: {
            color: colors.text,
            fontWeight: 'bold',
        }
    });
};

export default AdminRequestsPage;