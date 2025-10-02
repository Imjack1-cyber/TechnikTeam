import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, Platform, Share } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing } from '../../styles/theme';
import ScrollableContent from '../../components/ui/ScrollableContent';
import AdminModal from '../../components/ui/AdminModal';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import AdminAvailabilityPollModal from '../../components/admin/availability/AdminAvailabilityPollModal';
import Clipboard from '@react-native-clipboard/clipboard';

const AdminAvailabilityPage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/admin/availability'), []);
	const { data: polls, loading, error, reload } = useApi(apiCall);
    const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [deletingPoll, setDeletingPoll] = useState(null);
    const [isSubmittingDelete, setIsSubmittingDelete] = useState(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

    const handleDeletePress = (poll) => {
        setDeletingPoll(poll);
        setIsDeleteModalOpen(true);
    };

    const confirmDelete = async () => {
        if (!deletingPoll) return;
        setIsSubmittingDelete(true);
        try {
            await apiClient.delete(`/admin/availability/${deletingPoll.id}`);
            addToast('Umfrage gelöscht.', 'success');
            reload();
        } catch (e) {
            addToast(`Fehler: ${e.message}`, 'error');
        } finally {
            setIsSubmittingDelete(false);
            setIsDeleteModalOpen(false);
            setDeletingPoll(null);
        }
    };
    
    const handleCreationSuccess = () => {
        setIsCreateModalOpen(false);
        reload();
    };

    const getBaseShareUrl = () => {
        if (Platform.OS === 'web') {
            return window.location.origin;
        }
        const mode = useAuthStore.getState().backendMode;
        const host = mode === 'dev' ? 'technikteamdev.qs0.de' : 'technikteam.qs0.de';
        return `https://${host}`;
    };

    const handleShare = async (poll) => {
        const pollUrl = `${getBaseShareUrl()}/poll/${poll.uuid}`;
        const shareMessage = `Bitte nimm an der Umfrage "${poll.title}" teil: ${pollUrl}`;
        const shareTitle = `Umfrage: ${poll.title}`;

        if (Platform.OS === 'web') {
            Clipboard.setString(pollUrl);
            addToast('Link in die Zwischenablage kopiert!', 'success');
        } else {
             try {
                await Share.share({
                    message: shareMessage,
                    url: pollUrl, // For iOS
                    title: shareTitle // For Android
                });
            } catch (error) {
                addToast(`Fehler beim Teilen: ${error.message}`, 'error');
            }
        }
    };


    const renderItem = ({ item }) => {
        return (
            <View style={styles.card}>
                <View style={{flexDirection: 'row', justifyContent: 'space-between'}}>
                    <Text style={styles.cardTitle}>{item.title}</Text>
                    <TouchableOpacity onPress={() => handleShare(item)}>
                        <Icon name="share-alt" size={20} color={colors.primary} />
                    </TouchableOpacity>
                </View>
                <View style={styles.detailsListRow}>
                    <Text style={styles.detailsListLabel}>Typ:</Text>
                    <Text style={styles.detailsListValue}>{item.type}</Text>
                </View>
                <View style={styles.detailsListRow}>
                    <Text style={styles.detailsListLabel}>Zeitraum:</Text>
                    <Text style={styles.detailsListValue}>{new Date(item.startTime).toLocaleString('de-DE')} - {new Date(item.endTime).toLocaleString('de-DE')}</Text>
                </View>
                 <View style={{flexDirection: 'row', justifyContent: 'flex-end', gap: spacing.sm, marginTop: spacing.md}}>
                    <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={() => navigation.navigate('AdminAvailabilityPollDetails', { pollId: item.id })}>
                        <Text style={styles.buttonText}>Ergebnisse</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => handleDeletePress(item)}>
                        <Text style={styles.dangerOutlineButtonText}>Löschen</Text>
                    </TouchableOpacity>
                </View>
            </View>
        );
    };

	return (
		<ScrollableContent>
            <View style={{padding: spacing.md}}>
			    <Text style={styles.title}>Verfügbarkeits-Check</Text>
			    <Text style={styles.subtitle}>Erstellen Sie Umfragen zur Verfügbarkeit des Teams und werten Sie diese aus.</Text>
                <TouchableOpacity style={[styles.button, styles.successButton, {alignSelf: 'flex-start'}]} onPress={() => setIsCreateModalOpen(true)}>
                    <Text style={styles.buttonText}>Neue Umfrage erstellen</Text>
                </TouchableOpacity>
            </View>
            {loading && <ActivityIndicator />}
            {error && <Text style={styles.errorText}>{error}</Text>}
            <FlatList 
                data={polls}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{paddingHorizontal: spacing.md}}
                ListEmptyComponent={<View style={styles.card}><Text>Keine Umfragen gefunden.</Text></View>}
            />
            {deletingPoll && (
                <ConfirmationModal
                    isOpen={isDeleteModalOpen}
                    onClose={() => setIsDeleteModalOpen(false)}
                    onConfirm={confirmDelete}
                    title="Umfrage löschen?"
                    message={`Die Umfrage "${deletingPoll.title}" und alle Antworten werden permanent gelöscht.`}
                    confirmText="Löschen"
                    isSubmitting={isSubmittingDelete}
                />
            )}
            <AdminAvailabilityPollModal 
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                onSuccess={handleCreationSuccess}
            />
		</ScrollableContent>
	);
};

export default AdminAvailabilityPage;