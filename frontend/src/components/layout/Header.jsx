import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Platform } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { DrawerActions } from '@react-navigation/native';
import { getThemeColors, spacing } from '../../styles/theme';
import { navigationRef } from '../../router/navigation';
import SharePageModal from '../ui/SharePageModal';

const Header = ({ navigation, route, options, back }) => {
	const { user, theme, backendMode } = useAuthStore(state => ({
        user: state.user,
        theme: state.theme,
        backendMode: state.backendMode
    }));
	const unseenCount = user?.unseenNotificationsCount || 0;
    const insets = useSafeAreaInsets();
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);
    const [isShareModalOpen, setIsShareModalOpen] = useState(false);
    const [currentPageUrl, setCurrentPageUrl] = useState('');

    const title = options?.title ?? route.name;

    const handleShare = () => {
        const currentRoute = navigationRef.getCurrentRoute();
        if (!currentRoute) return;

        let baseUrl = '';
        if (Platform.OS === 'web') {
            baseUrl = window.location.origin;
        } else {
            const baseDomain = backendMode === 'dev' ? 'technikteamdev.qs0.de' : 'technikteam.qs0.de';
            baseUrl = `https://${baseDomain}`;
        }
        
        let path = '';

        // Manually map all known Drawer and non-drawer Stack screens to their canonical deep link paths
        switch (currentRoute.name) {
            // User Pages
            case 'Dashboard': path = '/home'; break;
            case 'Anschlagbrett': path = '/bulletin-board'; break;
            case 'Benachrichtigungen': path = '/notifications'; break;
            case 'Team': path = '/team'; break;
            case 'Chat': path = '/chat'; break;
            case 'Lehrgänge': path = '/lehrgaenge'; break;
            case 'EventsList': path = '/veranstaltungen'; break;
            case 'EventDetails': path = `/veranstaltungen/details/${currentRoute.params.eventId}`; break;
            case 'Lager': path = '/lager'; break;
            case 'Dateien': path = '/dateien'; break;
            case 'Kalender': path = '/kalender'; break;
            case 'Feedback': path = '/feedback'; break;
            case 'Changelogs': path = '/changelogs'; break;
            case 'Profile': path = '/profil'; break;
            case 'UserProfile': path = `/team/${currentRoute.params.userId}`; break;
            case 'MeetingDetails': path = `/lehrgaenge/details/${currentRoute.params.meetingId}`; break;
            case 'StorageItemDetails': path = `/lager/details/${currentRoute.params.itemId}`; break;
            case 'Search': path = `/suche?q=${currentRoute.params.q}`; break;
            
            // Admin Pages
            case 'AdminDashboardPage': path = '/admin/dashboard'; break;
            case 'AdminUsers': path = '/admin/users/manage'; break;
            case 'AdminRequests': path = '/admin/users/requests'; break;
            case 'AdminEvents': path = '/admin/events/manage'; break;
            case 'AdminStorage': path = '/admin/storage/manage'; break;
            case 'AdminCourses': path = '/admin/courses/manage'; break;
            case 'AdminMatrix': path = '/admin/courses/matrix'; break;
            case 'AdminReports': path = '/admin/reports/overview'; break;
            case 'AdminLog': path = '/admin/reports/log'; break;
            case 'AdminSystemPage': path = '/admin/system/status'; break;
            case 'AdminAuthLog': path = '/admin/system/auth-log'; break;

            default:
                console.warn(`[Header] No specific deep link path defined for route: ${currentRoute.name}. Defaulting to /home`);
                path = '/home'; 
                break;
        }

        setCurrentPageUrl(`${baseUrl}${path}`);
        setIsShareModalOpen(true);
    };

	return (
        <>
            <View style={[styles.header, { paddingTop: Platform.OS === 'ios' ? insets.top : 10, height: (Platform.OS === 'ios' ? insets.top : 10) + 54 }]}>
                {back ? (
                    <TouchableOpacity style={styles.toggleButton} onPress={navigation.goBack}>
                        <Icon name={'arrow-left'} size={24} color={colors.text} />
                    </TouchableOpacity>
                ) : (
                    <TouchableOpacity style={styles.toggleButton} onPress={() => navigation.dispatch(DrawerActions.toggleDrawer()) }>
                        <Icon name={'bars'} size={24} color={colors.text} />
                    </TouchableOpacity>
                )}
                <View>
                    <Text style={styles.logo}>{title}</Text>
                </View>
                <View style={styles.rightContainer}>
                    <TouchableOpacity onPress={handleShare} style={styles.iconButton}>
                        <Icon name="share-alt" solid size={22} color={colors.text} />
                    </TouchableOpacity>
                    <TouchableOpacity onPress={() => navigation.navigate('Benachrichtigungen')} style={styles.iconButton}>
                        <Icon name="bell" solid size={24} color={colors.text} />
                        {unseenCount > 0 && (
                            <View style={styles.badge}>
                                <Text style={styles.badgeText}>{unseenCount}</Text>
                            </View>
                        )}
                    </TouchableOpacity>
                    <TouchableOpacity onPress={() => navigation.navigate('Profile')} style={styles.iconButton}>
                        <Icon name={user?.profileIconClass?.replace('fa-', '') || 'user-circle'} solid size={24} color={colors.text} />
                    </TouchableOpacity>
                </View>
            </View>
            <SharePageModal 
                isOpen={isShareModalOpen}
                onClose={() => setIsShareModalOpen(false)}
                url={currentPageUrl}
            />
        </>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: {
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'space-between',
            backgroundColor: colors.surface,
            paddingHorizontal: 16,
            paddingBottom: 10,
            borderBottomWidth: 1,
            borderBottomColor: colors.border,
        },
        toggleButton: {
            padding: 8,
        },
        logo: {
            fontWeight: '600',
            fontSize: 18,
            color: colors.heading,
        },
        rightContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            gap: 16,
        },
        iconButton: {
            padding: 8,
        },
        badge: {
            position: 'absolute',
            top: 0,
            right: 0,
            backgroundColor: colors.danger,
            borderRadius: 10,
            width: 20,
            height: 20,
            justifyContent: 'center',
            alignItems: 'center',
            borderWidth: 2,
            borderColor: colors.surface,
        },
        badgeText: {
            color: colors.white,
            fontSize: 10,
            fontWeight: 'bold',
        },
    });
};

export default Header;