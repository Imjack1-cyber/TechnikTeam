import React, { useCallback, useEffect, useState, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing, borders } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';
import { navigateFromUrl } from '../router/navigationHelper';
import { useToast } from '../context/ToastContext';

const NotificationItem = ({ notification, styles, colors }) => {
    const navigation = useNavigation();

	const getIcon = (level) => {
		switch (level) {
			case 'Warning': return 'exclamation-triangle';
			case 'Important': return 'exclamation-circle';
			default: return 'info-circle';
		}
	};

	const getIconColor = (level) => {
		switch (level) {
			case 'Warning': return colors.danger;
			case 'Important': return colors.warning;
			default: return colors.info;
		}
	};
    
    const handlePress = () => {
        if (notification.url) {
            navigateFromUrl(notification.url);
        }
    };

	const content = (
		<View style={styles.itemContainer}>
			<View style={[styles.iconContainer, { backgroundColor: getIconColor(notification.level) }]}>
				<Icon name={getIcon(notification.level)} size={20} color={colors.white} />
			</View>
			<View style={styles.contentContainer}>
				<View style={styles.itemHeader}>
					<Text style={styles.itemTitle} numberOfLines={1}>{notification.title}</Text>
					<Text style={styles.itemDate}>{new Date(notification.createdAt).toLocaleDateString('de-DE')}</Text>
				</View>
				<Text style={styles.itemDescription}>{notification.description}</Text>
			</View>
            {notification.url && <Icon name="chevron-right" size={16} color={colors.textMuted} />}
		</View>
	);

	return (
        <TouchableOpacity onPress={handlePress} disabled={!notification.url}>
            {content}
        </TouchableOpacity>
    );
};


const NotificationsPage = () => {
	const { unseenNotificationsCount, setUnseenNotificationCount } = useAuthStore(state => ({
        unseenNotificationsCount: state.user?.unseenNotificationsCount || 0,
        setUnseenNotificationCount: state.setUnseenNotificationCount
    }));
	const apiCall = useCallback(() => apiClient.get('/public/notifications'), []);
	const { data: notifications, loading, error, reload } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const { addToast } = useToast();
    const [activeTab, setActiveTab] = useState('unread');

	useEffect(() => {
        // When the user enters the screen, clear the badge count in the header/sidebar.
        // We don't reload the data, so the "Unread" tab remains populated until they manually clear it or navigate away.
        if (unseenNotificationsCount > 0) {
            apiClient.post('/public/notifications/mark-all-seen')
                .then(() => {
                    setUnseenNotificationCount(0);
                })
                .catch(err => console.error("Failed to mark notifications as seen on mount", err));
        }
    }, [unseenNotificationsCount, setUnseenNotificationCount]);

    const displayedNotifications = useMemo(() => {
        if (!notifications) return [];
        if (activeTab === 'unread') {
            return notifications.filter(n => !n.seen);
        }
        return notifications; // "read" tab shows all
    }, [notifications, activeTab]);

    const hasUnseen = useMemo(() => notifications?.some(n => !n.seen), [notifications]);

    const handleMarkAllRead = async () => {
        try {
            const result = await apiClient.post('/public/notifications/mark-all-seen');
            if (result.success) {
                addToast('Alle Benachrichtigungen als gelesen markiert.', 'success');
                reload(); // Refresh the list to move items out of the unread tab
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast('Fehler beim Markieren der Benachrichtigungen.', 'error');
        }
    };


	return (
		<View style={styles.container}>
			<View style={styles.headerContainer}>
                <Icon name="bell" solid size={24} style={styles.headerIcon}/>
				<Text style={styles.title}>Benachrichtigungen</Text>
			</View>
            {hasUnseen && (
                <TouchableOpacity style={styles.markAllReadButton} onPress={handleMarkAllRead}>
                    <Text style={styles.markAllReadText}>Alle als gelesen markieren</Text>
                </TouchableOpacity>
            )}

            <View style={styles.tabContainer}>
                <TouchableOpacity 
                    style={[styles.tabButton, activeTab === 'unread' && styles.activeTabButton]}
                    onPress={() => setActiveTab('unread')}
                >
                    <Text style={[styles.tabText, activeTab === 'unread' && styles.activeTabText]}>Ungelesen</Text>
                </TouchableOpacity>
                 <TouchableOpacity 
                    style={[styles.tabButton, activeTab === 'read' && styles.activeTabButton]}
                    onPress={() => setActiveTab('read')}
                >
                    <Text style={[styles.tabText, activeTab === 'read' && styles.activeTabText]}>Alle</Text>
                </TouchableOpacity>
            </View>

			{loading ? <ActivityIndicator size="large" style={{marginTop: 40}} /> : 
             error ? <Text style={styles.errorText}>{error}</Text> : (
                <FlatList
                    data={displayedNotifications}
                    keyExtractor={item => item.id.toString()}
                    renderItem={({item}) => <NotificationItem notification={item} styles={styles} colors={colors} />}
                    ListEmptyComponent={
                        <View style={styles.emptyContainer}>
                            <Icon name="check-circle" solid size={48} color={colors.success} />
                            <Text style={styles.emptyText}>
                                {activeTab === 'unread' ? 'Keine ungelesenen Benachrichtigungen.' : 'Keine Benachrichtigungen vorhanden.'}
                            </Text>
                        </View>
                    }
                />
            )}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            padding: spacing.md,
        },
        headerIcon: {
            color: colors.heading,
            marginRight: spacing.sm,
        },
        markAllReadButton: {
            alignSelf: 'flex-start',
            marginHorizontal: spacing.md,
            marginBottom: spacing.md,
            paddingVertical: spacing.xs,
            paddingHorizontal: spacing.sm,
            backgroundColor: colors.primaryLight,
            borderRadius: borders.radius,
        },
        markAllReadText: {
            color: colors.primary,
            fontWeight: '500',
        },
        tabContainer: {
            flexDirection: 'row',
            borderBottomWidth: 1,
            borderColor: colors.border,
            marginHorizontal: spacing.md,
        },
        tabButton: {
            flex: 1,
            paddingVertical: spacing.md,
            alignItems: 'center',
            borderBottomWidth: 3,
            borderBottomColor: 'transparent',
        },
        activeTabButton: {
            borderBottomColor: colors.primary,
        },
        tabText: {
            fontSize: typography.body,
            color: colors.textMuted,
            fontWeight: '500',
        },
        activeTabText: {
            color: colors.primary,
        },
        itemContainer: { 
            flexDirection: 'row', 
            alignItems: 'center', 
            padding: spacing.md, 
            borderBottomWidth: 1, 
            borderColor: colors.border,
            backgroundColor: colors.surface,
        },
        iconContainer: {
            width: 40,
            height: 40,
            borderRadius: 20,
            justifyContent: 'center',
            alignItems: 'center',
            marginRight: spacing.md,
        },
        contentContainer: {
            flex: 1,
        },
        itemHeader: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: spacing.xs,
        },
		itemTitle: { 
            fontWeight: 'bold', 
            fontSize: typography.body, 
            color: colors.text,
            flex: 1,
            marginRight: spacing.sm,
        },
		itemDate: { 
            fontSize: typography.caption, 
            color: colors.textMuted 
        },
		itemDescription: { 
            color: colors.textMuted 
        },
        emptyContainer: {
            alignItems: 'center',
            paddingTop: spacing.xl * 2,
        },
        emptyText: {
            marginTop: spacing.md,
            fontSize: typography.h4,
            color: colors.textMuted,
        }
    });
};

export default NotificationsPage;