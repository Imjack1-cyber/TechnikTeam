import React, { useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import ProfileQualifications from '../components/profile/ProfileQualifications';
import ProfileAchievements from '../components/profile/ProfileAchievements';
import ProfileEventHistory from '../components/profile/ProfileEventHistory';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import Icon from 'react-native-vector-icons/FontAwesome5';

const UserProfilePage = () => {
    const navigation = useNavigation();
	const route = useRoute();
	const { userId } = route.params;
	const apiCall = useCallback(() => apiClient.get(`/public/profile/${userId}`), [userId]);
	const { data: profileData, loading, error } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	if (!profileData) return <View style={styles.centered}><Text>Keine Profildaten für diesen Benutzer gefunden.</Text></View>;

	const { user, eventHistory, qualifications, achievements } = profileData;

	return (
		<ScrollView style={styles.container}>
			<View style={{flexDirection: 'row', alignItems: 'center', gap: 16, padding: 16}}>
				<Icon name={user.profileIconClass?.replace('fa-', '') || 'user-circle'} solid size={80} color="#6c757d" />
				<View>
					<Text style={styles.title}>{user.username}</Text>
					<Text style={styles.subtitle}>{user.roleName}</Text>
				</View>
			</View>
			<ProfileQualifications qualifications={qualifications} />
			<ProfileAchievements achievements={achievements} />
			<ProfileEventHistory eventHistory={eventHistory} />

			<TouchableOpacity style={[styles.button, styles.secondaryButton, {margin: 16}]} onPress={() => navigation.goBack()}>
				<Icon name="arrow-left" size={14} color="#fff" />
				<Text style={styles.buttonText}>Zurück</Text>
			</TouchableOpacity>
		</ScrollView>
	);
};

export default UserProfilePage;