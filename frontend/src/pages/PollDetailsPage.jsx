import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, ScrollView } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../styles/theme';
import { RadioButton } from 'react-native-paper';
import ProgressBar from '../components/ui/ProgressBar';
import { useToast } from '../context/ToastContext';
import ConfirmationModal from '../components/ui/ConfirmationModal';
import Icon from 'react-native-vector-icons/FontAwesome5';

const PollDetailsPage = () => {
    const route = useRoute();
    const { pollId } = route.params;
    const { user, isAdmin } = useAuthStore();
    const canManagePolls = isAdmin || user?.permissions.includes('POLL_MANAGE');
    const { addToast } = useToast();
    const navigation = useNavigation();

    const apiCall = useCallback(() => apiClient.get(`/public/polls/${pollId}`), [pollId]);
    const { data: poll, loading, error, reload } = useApi(apiCall, { subscribeTo: 'POLL' });

    const [selectedOption, setSelectedOption] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [actionablePoll, setActionablePoll] = useState(null);
    const [actionType, setActionType] = useState(null);

    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const handleVote = async () => {
        if (!selectedOption) return;
        setIsSubmitting(true);
        try {
            const result = await apiClient.post(`/public/polls/${pollId}/vote`, { poll_option_id: selectedOption });
            if (result.success) {
                addToast('Stimme erfolgreich abgegeben.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleAdminAction = async () => {
        if (!actionablePoll || !actionType) return;

        setIsSubmitting(true);
        try {
            let result;
            if (actionType === 'close') {
                result = await apiClient.put(`/admin/polls/${actionablePoll.id}`, { isClosed: true });
            } else { // delete
                result = await apiClient.delete(`/admin/polls/${actionablePoll.id}`);
            }

            if (result.success) {
                addToast(`Umfrage erfolgreich ${actionType === 'close' ? 'geschlossen' : 'gelöscht'}.`, 'success');
                if (actionType === 'delete') {
                    navigation.goBack();
                } else {
                    reload();
                }
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
        finally {
            setIsSubmitting(false);
            setActionablePoll(null);
            setActionType(null);
        }
    };

    if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
    if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
    if (!poll) return <View style={styles.centered}><Text>Umfrage nicht gefunden.</Text></View>;

    const isPollActive = !poll.isClosed && (!poll.closesAt || new Date(poll.closesAt) > new Date());
    const showResults = !isPollActive || poll.hasVoted;

    return (
        <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
            <View style={styles.header}>
                <Text style={styles.title}>{poll.question}</Text>
                <Text style={styles.subtitle}>Erstellt von {poll.createdByUsername}</Text>
            </View>
            
            {showResults ? (
                <View style={styles.card}>
                    <Text style={styles.cardTitle}>Ergebnisse</Text>
                    {poll.pollOptions?.map(option => (
                        <View key={option.id} style={styles.resultRow}>
                            <View style={{flexDirection: 'row', justifyContent: 'space-between', marginBottom: spacing.xs}}>
                                <Text style={styles.optionText}>{option.optionText}</Text>
                                <Text style={styles.voteCount}>{option.voteCount} Stimme(n) ({option.votePercentage.toFixed(1)}%)</Text>
                            </View>
                            <ProgressBar progress={option.votePercentage / 100} />
                        </View>
                    ))}
                </View>
            ) : (
                <View style={styles.card}>
                    <Text style={styles.cardTitle}>Stimme abgeben</Text>
                    <RadioButton.Group onValueChange={newValue => setSelectedOption(newValue)} value={selectedOption}>
                        {poll.pollOptions?.map(option => (
                            <View key={option.id} style={{ flexDirection: 'row', alignItems: 'center' }}>
                                <RadioButton value={option.id} />
                                <Text>{option.optionText}</Text>
                            </View>
                        ))}
                    </RadioButton.Group>
                    <TouchableOpacity style={[styles.button, styles.successButton, {marginTop: spacing.md}]} onPress={handleVote} disabled={isSubmitting}>
                        <Text style={styles.buttonText}>Abstimmen</Text>
                    </TouchableOpacity>
                </View>
            )}

            {canManagePolls && (
                <View style={[styles.card, styles.adminCard]}>
                    <Text style={styles.cardTitle}>Admin-Aktionen</Text>
                    <View style={{flexDirection: 'row', gap: spacing.sm}}>
                        {!poll.isClosed && (
                            <TouchableOpacity style={[styles.button, {backgroundColor: colors.warning}]} onPress={() => {setActionablePoll(poll); setActionType('close');}}>
                                <Text style={{color: '#000'}}>Schließen</Text>
                            </TouchableOpacity>
                        )}
                        <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => {setActionablePoll(poll); setActionType('delete');}}>
                            <Text style={styles.buttonText}>Löschen</Text>
                        </TouchableOpacity>
                    </View>
                </View>
            )}
             <ConfirmationModal
                isOpen={!!actionablePoll}
                onClose={() => setActionablePoll(null)}
                onConfirm={handleAdminAction}
                title={`Umfrage "${actionablePoll.question}" ${actionType === 'close' ? 'schließen' : 'löschen'}?`}
                message="Diese Aktion kann nicht rückgängig gemacht werden."
                confirmText={actionType === 'close' ? 'Schließen' : 'Löschen'}
                confirmButtonVariant="danger"
                isSubmitting={isSubmitting}
            />
        </ScrollView>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: { marginBottom: spacing.md },
        resultRow: { marginBottom: spacing.md },
        optionText: { fontSize: typography.body, fontWeight: '500' },
        voteCount: { color: colors.textMuted },
        adminCard: { marginTop: spacing.lg, borderColor: colors.danger, borderWidth: 2 },
    });
};

export default PollDetailsPage;