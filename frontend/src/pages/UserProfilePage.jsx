import React, { useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator, TouchableOpacity } from 'react-native';
import { useRoute } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import ProfileQualifications from '../components/profile/ProfileQualifications';
import ProfileAchievements from '../components/profile/ProfileAchievements';
import ProfileEventHistory from '../components/profile/ProfileEventHistory';
import Icon from 'react-native-vector-icons/FontAwesome5';

const UserProfilePage = ({ navigation }) => {
	const route = useRoute();
	const { userId } = route.params;

	const apiCall = useCallback(() => apiClient.get(`/public/profile/${userId}`), [userId]);
	const { data: profileData, loading, error } = useApi(apiCall);

	if (loading) {
		return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	}

	if (error) {
		return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	}

	if (!profileData) {
		return <View style={styles.centered}><Text>Keine Profildaten für diesen Benutzer gefunden.</Text></View>;
	}

	const { user, eventHistory, qualifications, achievements } = profileData;

	return (
		<ScrollView style={styles.container}>
			<View style={styles.header}>
				<Icon name={user.profileIconClass?.replace('fa-', '') || 'user-circle'} solid size={80} color="#6c757d" />
				<View style={styles.headerText}>
					<Text style={styles.title}>{user.username}</Text>
					<Text style={styles.subtitle}>{user.roleName}</Text>
				</View>
			</View>

			<ProfileQualifications qualifications={qualifications} />
			<ProfileAchievements achievements={achievements} />
			<ProfileEventHistory eventHistory={eventHistory} />

			<TouchableOpacity style={styles.backButton} onPress={() => navigation.goBack()}>
                <Icon name="arrow-left" size={14} color="#fff" />
				<Text style={styles.backButtonText}>Zurück zum Team-Verzeichnis</Text>
			</TouchableOpacity>
		</ScrollView>
	);
};

const styles = StyleSheet.create({
	container: {
		flex: 1,
		backgroundColor: '#f8f9fa',
	},
	centered: {
		flex: 1,
		justifyContent: 'center',
		alignItems: 'center',
	},
	header: {
		flexDirection: 'row',
		alignItems: 'center',
		gap: 16,
		marginBottom: 16,
		padding: 16,
	},
	headerText: {
		flex: 1,
	},
	title: {
		fontSize: 24,
		fontWeight: '700',
		color: '#002B5B',
	},
	subtitle: {
		fontSize: 16,
		color: '#6c757d',
	},
	backButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
		backgroundColor: '#6c757d',
		paddingVertical: 12,
		borderRadius: 6,
		margin: 16,
	},
	backButtonText: {
		color: '#fff',
		fontWeight: '500',
        fontSize: 16,
	},
	errorText: {
		color: '#dc3545',
	},
});


export default UserProfilePage;