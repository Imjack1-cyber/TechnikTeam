import React, { useCallback, useState, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useToast } from '../context/ToastContext';
import Modal from '../components/ui/Modal';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../store/authStore';

const LehrgaengePage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/public/meetings'), []);
	const { data: coursesWithMeetings, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const handleAction = async (meetingId, action) => {
        try {
			const result = await apiClient.post(`/public/meetings/${meetingId}/${action}`, {});
			if (result.success) {
				addToast(result.message, 'success');
				reload();
			} else { throw new Error(result.message); }
		} catch (err) { addToast(err.message || 'Aktion fehlgeschlagen.', 'error'); }
	};
    
    const allMeetings = useMemo(() => {
        if (!coursesWithMeetings) return [];
        return coursesWithMeetings.flatMap(course => course.upcomingMeetings || []);
    }, [coursesWithMeetings]);

    const renderMeetingItem = ({ item: meeting }) => {
        const action = meeting.userAttendanceStatus === 'ANGEMELDET' ? 'signoff' : 'signup';
        const isSignedUp = meeting.userAttendanceStatus === 'ANGEMELDET';

        return (
            <View style={styles.card}>
                <TouchableOpacity onPress={() => navigation.navigate('MeetingDetails', { meetingId: meeting.id })}>
                    <Text style={styles.cardTitle}>{meeting.parentCourseName}: {meeting.name}</Text>
                </TouchableOpacity>
                <View style={styles.detailRow}>
                    <Text style={styles.label}>Datum:</Text>
                    <Text style={styles.value}>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</Text>
                </View>
                 <View style={styles.detailRow}>
                    <Text style={styles.label}>Leitung:</Text>
                    <Text style={styles.value}>{meeting.leaderUsername || 'N/A'}</Text>
                </View>
                <View style={styles.detailRow}>
                    <Text style={styles.label}>Teilnehmer:</Text>
                    <Text style={styles.value}>{meeting.participantCount || 0} / {meeting.maxParticipants || '∞'}</Text>
                </View>
                <View style={[styles.detailRow, { borderBottomWidth: 0 }]}>
                    <Text style={styles.label}>Dein Status:</Text>
                    <Text style={[styles.value, isSignedUp && { color: colors.success, fontWeight: 'bold' }]}>{meeting.userAttendanceStatus}</Text>
                </View>
                <TouchableOpacity 
                    style={[styles.button, isSignedUp ? styles.dangerButton : styles.successButton, {marginTop: 16}]} 
                    onPress={() => handleAction(meeting.id, action)}
                >
                    <Text style={styles.buttonText}>{isSignedUp ? 'Abmelden' : 'Anmelden'}</Text>
                </TouchableOpacity>
            </View>
        );
    };

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large"/></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;

	return (
        <View style={styles.container}>
            <FlatList
                data={allMeetings}
                keyExtractor={item => item.id.toString()}
                renderItem={renderMeetingItem}
                contentContainerStyle={styles.contentContainer}
                ListHeaderComponent={
                    <View style={styles.headerContainer}>
                        <Icon name="graduation-cap" size={24} style={styles.headerIcon}/>
                        <Text style={styles.title}>Lehrgangs-Hub</Text>
                    </View>
                }
                ListEmptyComponent={
                    <View style={styles.card}>
                        <Text style={styles.bodyText}>Aktuell sind keine Lehrgänge oder Termine geplant.</Text>
                    </View>
                }
            />
        </View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { padding: spacing.md, flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: spacing.sm },
    });
};


export default LehrgaengePage;