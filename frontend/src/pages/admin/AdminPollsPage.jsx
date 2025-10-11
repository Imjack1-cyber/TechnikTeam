import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Platform, TextInput } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing, borders } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import PollCreateModal from '../../components/polls/PollCreateModal';
import ConfirmationModal from '../../components/ui/ConfirmationModal';
import { useToast } from '../../context/ToastContext';
import ShareModal from '../../components/ui/ShareModal';
import AdminModal from '../../components/ui/AdminModal';
import DateTimePicker from '../../components/ui/DateTimePicker';
import CustomPicker from '../../components/ui/CustomPicker';

const FilterModal = ({ isOpen, onClose, filters, setFilters, uniqueUsers }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const [localFilters, setLocalFilters] = useState(filters);

    useEffect(() => {
        if (isOpen) {
            setLocalFilters(filters);
        }
    }, [isOpen, filters]);

    const handleApply = () => {
        setFilters(localFilters);
        onClose();
    };

    const typeOptions = [
        { label: 'Alle Typen', value: 'ALL' },
        { label: 'Planung (Verfügbarkeit/Terminfindung)', value: 'SCHEDULING' },
        { label: 'Intern (Multiple Choice/Wortwolke)', value: 'INTERNAL' },
    ];
    
    const statusOptions = [
        { label: 'Alle Status', value: 'ALL' },
        { label: 'Aktiv', value: 'ACTIVE' },
        { label: 'Geschlossen', value: 'CLOSED' },
    ];

    const creatorOptions = [
        { label: 'Alle Ersteller', value: '' },
        ...uniqueUsers.map(u => ({ label: u, value: u }))
    ];

    return (
        <AdminModal isOpen={isOpen} onClose={onClose} title="Umfragen filtern" onSubmit={handleApply} submitText="Filter anwenden">
            <CustomPicker
                label="Umfragetyp"
                selectedValue={localFilters.type}
                onValueChange={(val) => setLocalFilters({...localFilters, type: val})}
                options={typeOptions}
            />
            <CustomPicker
                label="Status"
                selectedValue={localFilters.status}
                onValueChange={(val) => setLocalFilters({...localFilters, status: val})}
                options={statusOptions}
            />
             <CustomPicker
                label="Ersteller"
                selectedValue={localFilters.creator}
                onValueChange={(val) => setLocalFilters({...localFilters, creator: val})}
                options={creatorOptions}
            />
            <DateTimePicker label="Erstellt nach" value={localFilters.startDate} onChange={date => setLocalFilters({...localFilters, startDate: date})} mode="date" />
            <DateTimePicker label="Erstellt vor" value={localFilters.endDate} onChange={date => setLocalFilters({...localFilters, endDate: date})} mode="date" />
        </AdminModal>
    );
};


const AdminPollsPage = () => {
    const navigation = useNavigation();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const { addToast } = useToast();

    const apiCall = useCallback(() => apiClient.get('/admin/polls'), []);
    const { data: polls, loading, error, reload } = useApi(apiCall, { subscribeTo: 'POLL' });

    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const [actionablePoll, setActionablePoll] = useState(null);
    const [editingPoll, setEditingPoll] = useState(null);
    const [actionType, setActionType] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [sharingPoll, setSharingPoll] = useState(null);
    const [isFilterModalOpen, setIsFilterModalOpen] = useState(false);
    const [filters, setFilters] = useState({ type: 'ALL', status: 'ALL', creator: '', startDate: null, endDate: null });

    const uniqueUsers = useMemo(() => {
        if (!polls) return [];
        return [...new Set(polls.map(p => p.createdByUsername))].sort();
    }, [polls]);

    const filteredPolls = useMemo(() => {
        if (!polls) return [];
        return polls.filter(p => {
            const matchesType = filters.type === 'ALL' ||
                (filters.type === 'INTERNAL' && (p.type === 'MULTIPLE_CHOICE' || p.type === 'WORD_CLOUD')) ||
                (filters.type === 'SCHEDULING' && (p.type === 'SCHEDULING' || p.type === 'AVAILABILITY'));
            const matchesStatus = filters.status === 'ALL' ||
                (filters.status === 'ACTIVE' && !p.isClosed) ||
                (filters.status === 'CLOSED' && p.isClosed);
            const matchesCreator = !filters.creator || p.createdByUsername === filters.creator;
            const matchesStartDate = !filters.startDate || new Date(p.createdAt) >= filters.startDate;
            const matchesEndDate = !filters.endDate || new Date(p.createdAt) <= filters.endDate;

            return matchesType && matchesStatus && matchesCreator && matchesStartDate && matchesEndDate;
        });
    }, [polls, filters]);

    const handleAction = async () => {
        if (!actionablePoll || !actionType) return;

        setIsSubmitting(true);
        try {
            let result;
            if (actionType === 'close') {
                result = await apiClient.put(`/admin/polls/${actionablePoll.id}`, { ...actionablePoll, isClosed: true });
            } else { // delete
                result = await apiClient.delete(`/admin/polls/${actionablePoll.id}`);
            }

            if (result.success) {
                addToast(`Umfrage erfolgreich ${actionType === 'close' ? 'geschlossen' : 'gelöscht'}.`, 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
        finally {
            setIsSubmitting(false);
            setActionablePoll(null);
            setActionType(null);
        }
    };
    
    const getBaseShareUrl = () => {
        if (Platform.OS === 'web') {
            return window.location.origin;
        }
        const mode = useAuthStore.getState().backendMode;
        const host = mode === 'dev' ? 'technikteamdev.qs0.de' : 'technikteam.qs0.de';
        return `https://${host}`;
    };

    const openEditModal = (poll = null) => {
        setEditingPoll(poll);
        setIsModalOpen(true);
    };

    const handleSuccess = () => {
        setIsModalOpen(false);
        setEditingPoll(null);
        reload();
    };

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>{item.question}</Text>
            <View style={styles.metaContainer}>
                <Text style={styles.metaText}>Typ: {item.type}</Text>
                <Text style={styles.metaText}>{item.isClosed ? "Geschlossen" : "Aktiv"}</Text>
            </View>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={() => navigation.navigate('AdminPollResults', { pollId: item.id })}>
                    <Text style={styles.buttonText}>Ergebnisse</Text>
                </TouchableOpacity>
                 <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => setSharingPoll(item)}>
                    <Text style={styles.buttonText}>Teilen</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openEditModal(item)}>
                    <Text style={styles.buttonText}>Bearbeiten</Text>
                </TouchableOpacity>
                {!item.isClosed && (
                    <TouchableOpacity style={[styles.button, {backgroundColor: colors.warning}]} onPress={() => {setActionablePoll(item); setActionType('close');}}>
                        <Text style={{color: '#000'}}>Schließen</Text>
                    </TouchableOpacity>
                )}
                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => {setActionablePoll(item); setActionType('delete');}}>
                    <Text style={styles.dangerOutlineButtonText}>Löschen</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

    return (
        <View style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="poll-h" size={24} style={styles.headerIcon} />
                <Text style={styles.title}>Umfragen verwalten</Text>
                <TouchableOpacity onPress={() => setIsFilterModalOpen(true)} style={{marginLeft: 'auto'}}>
                    <Icon name="filter" size={24} color={colors.primary} />
                </TouchableOpacity>
            </View>
            <TouchableOpacity style={[styles.button, styles.successButton, styles.createButton]} onPress={() => openEditModal()}>
                <Icon name="plus" size={16} color={colors.white} />
                <Text style={styles.buttonText}>Neue Umfrage</Text>
            </TouchableOpacity>

            {loading && <ActivityIndicator size="large" />}
            {error && <Text style={styles.errorText}>{error}</Text>}

            <FlatList
                data={filteredPolls}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{paddingHorizontal: spacing.md}}
                ListEmptyComponent={<Text style={styles.emptyText}>Keine Umfragen gefunden.</Text>}
            />

            {isModalOpen && (
                <PollCreateModal
                    isOpen={isModalOpen}
                    onClose={() => setIsModalOpen(false)}
                    onSuccess={handleSuccess}
                    poll={editingPoll}
                />
            )}
            {actionablePoll && (
                <ConfirmationModal
                    isOpen={!!actionablePoll}
                    onClose={() => setActionablePoll(null)}
                    onConfirm={handleAction}
                    title={`Umfrage "${actionablePoll.question}" ${actionType === 'close' ? 'schließen' : 'löschen'}?`}
                    message="Diese Aktion kann nicht rückgängig gemacht werden."
                    confirmText={actionType === 'close' ? 'Schließen' : 'Löschen'}
                    confirmButtonVariant="danger"
                    isSubmitting={isSubmitting}
                />
            )}
             {sharingPoll && (
                <ShareModal
                    isOpen={!!sharingPoll}
                    onClose={() => setSharingPoll(null)}
                    isCreatable={false}
                    itemType="poll"
                    itemId={sharingPoll.id}
                    itemName={sharingPoll.question}
                    shareUrl={`${getBaseShareUrl()}/poll/${sharingPoll.uuid}`}
                />
            )}
            <FilterModal
                isOpen={isFilterModalOpen}
                onClose={() => setIsFilterModalOpen(false)}
                filters={filters}
                setFilters={setFilters}
                uniqueUsers={uniqueUsers}
            />
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1, backgroundColor: colors.background },
        headerContainer: { padding: spacing.md, flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: spacing.sm },
        createButton: { marginHorizontal: spacing.md, marginBottom: spacing.md, alignSelf: 'flex-start' },
        metaContainer: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: spacing.sm },
        metaText: { fontSize: typography.caption, color: colors.textMuted },
        emptyText: { paddingHorizontal: spacing.md, color: colors.textMuted },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: spacing.sm, marginTop: spacing.md, flexWrap: 'wrap' },
    });
};

export default AdminPollsPage;