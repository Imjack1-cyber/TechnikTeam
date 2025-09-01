import React, { useCallback, useState } from 'react';
import { View, Text, ScrollView, TouchableOpacity, ActivityIndicator, StyleSheet } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import ProfileDetails from '../components/profile/ProfileDetails';
import ProfileSecurity from '../components/profile/ProfileSecurity';
import ProfileQualifications from '../components/profile/ProfileQualifications';
import ProfileAchievements from '../components/profile/ProfileAchievements';
import ProfileEventHistory from '../components/profile/ProfileEventHistory';
import ProfileLoginHistory from '../components/profile/ProfileLoginHistory';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing, typography } from '../styles/theme';
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
    const [activeTab, setActiveTab] = useState('overview');

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

	const { user, eventHistory = [], qualifications = [], achievements = [], hasPendingRequest = false, loginHistory = [] } = profileData;

    const renderTabContent = () => {
        switch (activeTab) {
            case 'security':
                return (
                    <>
                        <ProfileSecurity user={user} onUpdate={handleUpdate} />
                        <ProfileLoginHistory loginHistory={loginHistory} onUpdate={handleUpdate} />
                    </>
                );
            case 'activity':
                return (
                    <>
                        <ProfileAchievements achievements={achievements} />
                        <ProfileEventHistory eventHistory={eventHistory} />
                    </>
                );
            case 'overview':
            default:
                return (
                    <>
                        <ProfileDetails user={user} hasPendingRequest={hasPendingRequest} onUpdate={handleUpdate} />
                        <ProfileQualifications qualifications={qualifications} />
                    </>
                );
        }
    };

	return (
		<ScrollView style={styles.container}>
			<View style={styles.header}>
                <Icon name={user.profileIconClass?.replace('fa-', '') || 'user-circle'} solid size={80} color={colors.primary} />
                <View style={styles.headerTextContainer}>
				    <Text style={styles.title}>{user.username}</Text>
				    <Text style={styles.subtitle}>{user.roleName}</Text>
                </View>
				<TouchableOpacity style={styles.settingsButton} onPress={() => navigation.navigate('Settings')}>
					<Icon name="cog" size={24} color={colors.textMuted} />
				</TouchableOpacity>
			</View>

            <View style={styles.tabContainer}>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'overview' && styles.activeTab]} onPress={() => setActiveTab('overview')}>
                    <Text style={[styles.tabText, activeTab === 'overview' && styles.activeTabText]}>Übersicht</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'security' && styles.activeTab]} onPress={() => setActiveTab('security')}>
                    <Text style={[styles.tabText, activeTab === 'security' && styles.activeTabText]}>Sicherheit</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'activity' && styles.activeTab]} onPress={() => setActiveTab('activity')}>
                    <Text style={[styles.tabText, activeTab === 'activity' && styles.activeTabText]}>Aktivität</Text>
                </TouchableOpacity>
            </View>
			
            <View style={styles.mainContent}>
                {renderTabContent()}
            </View>
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: {
            flex: 1,
            backgroundColor: colors.background,
        },
        mainContent: {
            padding: spacing.md,
        },
        header: {
            flexDirection: 'row',
            alignItems: 'center',
            backgroundColor: colors.surface,
            padding: spacing.lg,
            borderBottomWidth: 1,
            borderBottomColor: colors.border,
            gap: spacing.md
        },
        headerTextContainer: {
            flex: 1,
        },
        settingsButton: {
            padding: spacing.sm,
        },
        title: {
            fontSize: typography.h1,
            fontWeight: 'bold',
            color: colors.heading,
            marginBottom: 0,
        },
        subtitle: {
             fontSize: typography.h4,
             color: colors.textMuted,
        },
        tabContainer: {
            flexDirection: 'row',
            paddingHorizontal: spacing.md,
            backgroundColor: colors.surface,
            borderBottomWidth: 1,
            borderColor: colors.border,
        },
        tabButton: {
            paddingVertical: spacing.md,
            paddingHorizontal: spacing.md,
            borderBottomWidth: 3,
            borderBottomColor: 'transparent',
        },
        activeTab: {
            borderBottomColor: colors.primary,
        },
        tabText: {
            fontSize: typography.body,
            fontWeight: '500',
            color: colors.textMuted,
        },
        activeTabText: {
            color: colors.primary,
        }
    });
};

export default ProfilePage;