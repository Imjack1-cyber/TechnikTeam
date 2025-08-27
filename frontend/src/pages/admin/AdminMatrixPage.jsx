import React, { useState, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import AttendanceModal from '../../components/admin/matrix/AttendanceModal';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';

const CELL_WIDTH = 120;
const USER_COL_WIDTH = 150;

const AdminMatrixPage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/matrix'), []);
	const { data, loading, error, reload } = useApi(apiCall);
	const qualificationsApiCall = useCallback(() => apiClient.get('/admin/qualifications/all'), []);
	const { data: allQualifications } = useApi(qualificationsApiCall);
	const [modalData, setModalData] = useState(null);
    
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const { users, courses, meetingsByCourse, attendanceMap, completionMap } = data || {};

	const openModal = (cellData) => {
        setModalData(cellData);
	};

    const handleSuccess = () => {
		setModalData(null);
		reload();
	};

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;

    const allMeetings = courses?.flatMap(course => meetingsByCourse[course.id] || [{id: `${course.id}-placeholder`, name: '-', courseId: course.id}]) || [];

	return (
		<View style={styles.container}>
			<Text style={styles.title}>Qualifikations-Matrix</Text>
			
            <View style={{flexDirection: 'row'}}>
                <View style={[styles.headerCell, {width: USER_COL_WIDTH}]}><Text style={styles.headerText}>Nutzer</Text></View>
                <ScrollView horizontal showsHorizontalScrollIndicator={false}>
                    <View>
                        <View style={{flexDirection: 'row'}}>
                            {courses?.map(course => (
                                <TouchableOpacity key={course.id} style={[styles.headerCell, styles.courseHeader, {width: (meetingsByCourse[course.id]?.length || 1) * CELL_WIDTH}]} onPress={() => navigation.navigate('AdminMeetings', {courseId: course.id})}>
                                    <Text style={styles.headerText}>{course.abbreviation}</Text>
                                </TouchableOpacity>
                            ))}
                        </View>
                        <View style={{flexDirection: 'row'}}>
                            {allMeetings.map(meeting => (
                                <TouchableOpacity key={meeting.id} style={[styles.headerCell, styles.meetingHeader, {width: CELL_WIDTH}]} onPress={() => navigation.navigate('MeetingDetails', {meetingId: meeting.id})}>
                                    <Text style={styles.headerText} numberOfLines={2}>{meeting.name}</Text>
                                </TouchableOpacity>
                            ))}
                        </View>
                    </View>
                </ScrollView>
            </View>

            <ScrollView>
                <View style={{flexDirection: 'row'}}>
                    <View style={{width: USER_COL_WIDTH}}>
                        {users?.map(user => (
                            <View key={user.id} style={[styles.cell, styles.userCell]}><Text style={styles.userText}>{user.username}</Text></View>
                        ))}
                    </View>
                    <ScrollView horizontal>
                         <View>
                            {users?.map(user => (
                                <View key={user.id} style={{flexDirection: 'row'}}>
                                    {courses?.map(course => {
                                        const hasCompletedCourse = completionMap[`${user.id}-${course.id}`];
                                        if(hasCompletedCourse) {
                                            return (
                                                <TouchableOpacity key={course.id} style={[styles.cell, {backgroundColor: colors.success, width: (meetingsByCourse[course.id]?.length || 1) * CELL_WIDTH}]}>
                                                    <Text style={{color: colors.white}}>Qualifiziert</Text>
                                                </TouchableOpacity>
                                            );
                                        }
                                        return (meetingsByCourse[course.id] || [{id: `${course.id}-placeholder`}]).map(meeting => {
                                             const attendance = attendanceMap[`${user.id}-${meeting.id}`];
                                             const attended = attendance ? attendance.attended : false;
                                             return (
                                                 <TouchableOpacity key={meeting.id} style={[styles.cell, {width: CELL_WIDTH}]} onPress={() => openModal({userId: user.id, meetingId: meeting.id /*... more data needed */})}>
                                                     {attended ? <Icon name="check" size={20} color={colors.success} /> : <Text style={{color: colors.textMuted}}>-</Text>}
                                                 </TouchableOpacity>
                                             );
                                        });
                                    })}
                                </View>
                            ))}
                        </View>
                    </ScrollView>
                </View>
            </ScrollView>

			{modalData && (
				<AttendanceModal isOpen={!!modalData} onClose={() => setModalData(null)} onSuccess={handleSuccess} cellData={modalData} />
			)}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerCell: { backgroundColor: colors.background, padding: spacing.sm, borderWidth: 1, borderColor: colors.border, justifyContent: 'center', alignItems: 'center' },
        headerText: { fontWeight: 'bold', textAlign: 'center' },
        courseHeader: { borderBottomWidth: 0 },
        meetingHeader: { height: 60 },
        cell: { height: 50, borderWidth: 1, borderColor: colors.border, justifyContent: 'center', alignItems: 'center' },
        userCell: { backgroundColor: colors.surface, alignItems: 'flex-start', paddingLeft: spacing.sm },
        userText: { fontWeight: '500' },
    });
};


export default AdminMatrixPage;