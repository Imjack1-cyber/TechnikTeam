import React, { useCallback, useState } from 'react';
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

const CourseAccordion = ({ course, onAction, onToggle, isOpen, styles, colors }) => {
    const navigation = useNavigation();
	
	return (
		<View style={styles.card}>
			<TouchableOpacity onPress={onToggle} style={styles.accordionHeader}>
				<View style={{flex: 1}}>
					<Text style={styles.cardTitle}>{course.name} ({course.abbreviation})</Text>
				</View>
				<Icon name={isOpen ? 'chevron-down' : 'chevron-right'} size={16} />
			</TouchableOpacity>

			{isOpen && (
				<View style={styles.accordionContent}>
					<Text>{course.description}</Text>
					<Text style={styles.subHeader}>Anstehende Termine:</Text>
					{course.upcomingMeetings.length === 0 ? (
						<Text>Keine Termine geplant.</Text>
					) : (
						course.upcomingMeetings.map(meeting => (
                            <View key={meeting.id} style={styles.meetingRow}>
                                <TouchableOpacity onPress={() => navigation.navigate('MeetingDetails', { meetingId: meeting.id })}>
                                    <Text style={{color: colors.primary}}>{meeting.name}</Text>
                                </TouchableOpacity>
                                <Text>{new Date(meeting.meetingDateTime).toLocaleDateString()}</Text>
                                <TouchableOpacity style={[styles.button, meeting.userAttendanceStatus === 'ANGEMELDET' ? styles.dangerButton : styles.successButton]} onPress={() => onAction(meeting.id, meeting.userAttendanceStatus === 'ANGEMELDET' ? 'signoff' : 'signup')}>
                                    <Text style={styles.buttonText}>{meeting.userAttendanceStatus === 'ANGEMELDET' ? 'Abmelden' : 'Anmelden'}</Text>
                                </TouchableOpacity>
                            </View>
                        ))
					)}
				</View>
			)}
		</View>
	);
};

const LehrgaengePage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/public/meetings'), []);
	const { data: courses, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();
	const [openCourseId, setOpenCourseId] = useState(null);
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

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large"/></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;

	return (
        <View style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="graduation-cap" size={24} style={styles.headerIcon}/>
                <Text style={styles.title}>Lehrgangs-Hub</Text>
            </View>
            <FlatList
                data={courses}
                keyExtractor={item => item.id.toString()}
                renderItem={({item}) => (
                    <CourseAccordion
                        course={item}
                        onAction={handleAction}
                        isOpen={openCourseId === item.id}
                        onToggle={() => setOpenCourseId(openCourseId === item.id ? null : item.id)}
                        styles={styles}
                        colors={colors}
                    />
                )}
                contentContainerStyle={styles.contentContainer}
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
        accordionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
        accordionContent: { marginTop: spacing.md, paddingTop: spacing.md, borderTopWidth: 1, borderColor: colors.border },
        subHeader: { fontSize: typography.h4, fontWeight: 'bold', marginTop: spacing.md },
        meetingRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: spacing.sm },
    });
};


export default LehrgaengePage;