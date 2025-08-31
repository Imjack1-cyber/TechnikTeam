import React, { useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, ActivityIndicator, StyleSheet } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import ProfileDetails from '../components/profile/ProfileDetails';
import ProfileSecurity from '../components/profile/ProfileSecurity';
import ProfileQualifications from '../components/profile/ProfileQualifications';
import ProfileAchievements from '../components/profile/ProfileAchievements';
import ProfileEventHistory from '../components/profile/ProfileEventHistory';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const ProfilePage = () => {
    const navigation = useNavigation();
	const { addToast } = useToast();
	const { fetchUserSession } = useAuthStore(state => ({
        fetchUserSession: state.fetchUserSession
    }));
	const apiCall = useCallback(() => apiClient.get('/public/profile'), []);
	const { data: profileData, loading, error, reload } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const handleUpdate = useCallback(() => {
		addToast('Profildaten werden aktualisiert...', 'info');
		reload();
		fetchUserSession();
	}, [reload, fetchUserSession, addToast]);

	if (loading) {
		return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	}

	if (error) {
		return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	}

	if (!profileData) {
		return <View style={styles.centered}><Text>Keine Profildaten gefunden.</Text></View>;
	}

	const { user, eventHistory, qualifications, achievements, hasPendingRequest } = profileData;

	return (
		<ScrollView style={styles.container}>
			<View style={styles.header}>
				<View style={{flexDirection: 'row', alignItems: 'center', gap: spacing.md}}>
                    <Icon name="user-circle" solid size={24} color={colors.heading} />
                    <Text style={styles.title}>Mein Profil</Text>
                </View>
				<TouchableOpacity onPress={() => navigation.navigate('Settings')}>
					<Icon name="cog" size={24} color={colors.textMuted} />
				</TouchableOpacity>
			</View>
			<Text style={styles.subtitle}>Hier finden Sie eine Übersicht Ihrer Daten, Qualifikationen und Aktivitäten.</Text>
			
            <ProfileDetails user={user} hasPendingRequest={hasPendingRequest} onUpdate={handleUpdate} />
            <ProfileSecurity onUpdate={handleUpdate} />
            <ProfileQualifications qualifications={qualifications} />
            <ProfileAchievements achievements={achievements} />
            <ProfileEventHistory eventHistory={eventHistory} />
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: spacing.md,
        },
    });
};

export default ProfilePage;