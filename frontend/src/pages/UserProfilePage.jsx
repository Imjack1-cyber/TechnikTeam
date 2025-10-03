import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import ProfileQualifications from '../components/profile/ProfileQualifications';
import ProfileAchievements from '../components/profile/ProfileAchievements';
import ProfileEventHistory from '../components/profile/ProfileEventHistory';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing, typography } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import ScrollableContent from '../components/ui/ScrollableContent';

const UserProfilePage = () => {
    const navigation = useNavigation();
	const route = useRoute();
	const { userId } = route.params;
	const apiCall = useCallback(() => apiClient.get(`/public/profile/${userId}`), [userId]);
	const { data: profileData, loading, error } = useApi(apiCall, { subscribeTo: 'USER' });
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const [activeTab, setActiveTab] = useState('activity');

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	if (!profileData) return <View style={styles.centered}><Text>Keine Profildaten für diesen Benutzer gefunden.</Text></View>;

	const { user, eventHistory, qualifications, achievements } = profileData;

    const renderTabContent = () => {
        switch (activeTab) {
            case 'qualifications':
                return <ProfileQualifications qualifications={qualifications} />;
            case 'activity':
            default:
                return (
                    <>
                        <ProfileAchievements achievements={achievements} />
                        <ProfileEventHistory eventHistory={eventHistory} />
                    </>
                );
        }
    };

	return (
		<ScrollableContent style={styles.container}>
			<View style={styles.header}>
                <Icon name={user.profileIconClass?.replace('fa-', '') || 'user-circle'} solid size={80} color={colors.primary} />
                <View style={styles.headerTextContainer}>
				    <Text style={styles.title}>{user.username}</Text>
				    <Text style={styles.subtitle}>{user.roleName}</Text>
                </View>
			</View>

            <View style={styles.tabContainer}>
                <TouchableOpacity 
                    style={[styles.tabButton, activeTab === 'activity' && styles.activeTabButton]} 
                    onPress={() => setActiveTab('activity')}>
                    <Text style={[styles.tabText, activeTab === 'activity' && styles.activeTabText]}>Aktivität</Text>
                </TouchableOpacity>
                <TouchableOpacity 
                    style={[styles.tabButton, activeTab === 'qualifications' && styles.activeTabButton]} 
                    onPress={() => setActiveTab('qualifications')}>
                    <Text style={[styles.tabText, activeTab === 'qualifications' && styles.activeTabText]}>Qualifikationen</Text>
                </TouchableOpacity>
            </View>
			
            <View style={styles.mainContent}>
                {renderTabContent()}
            </View>
		</ScrollableContent>
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
            paddingVertical: spacing.md,
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
        activeTabButton: {
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

export default UserProfilePage;