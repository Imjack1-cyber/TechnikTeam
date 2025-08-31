import React, { useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

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
            // Very basic URL parsing for this app's routes
            // This needs to be more robust in a real app
            const parts = notification.url.split('/').filter(Boolean);
            if (parts.length >= 2) {
                const routeName = parts[0].charAt(0).toUpperCase() + parts[0].slice(1); // e.g., 'veranstaltungen' -> 'Veranstaltungen'
                const detailRoute = `${routeName}Details`; // e.g., 'VeranstaltungenDetails'
                const paramName = `${parts[1]}Id`; // e.g., 'detailsId' -> simplified to 'eventId'
                const id = parts[2];
                // This is a simplified navigation logic
                // A real app would have a more robust URL parsing service
                if (routeName === 'Veranstaltungen') {
                    navigation.navigate('EventDetails', { eventId: id });
                }
            }
        }
    };

	const content = (
		<View style={[styles.itemContainer, !notification.seen && styles.unseenItem]}>
			<Icon name={getIcon(notification.level)} size={24} style={{ color: getIconColor(notification.level), marginRight: spacing.md }} />
			<View style={{ flex: 1 }}>
				<View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
					<Text style={styles.itemTitle}>{notification.title}</Text>
					<Text style={styles.itemDate}>{new Date(notification.createdAt).toLocaleString('de-DE')}</Text>
				</View>
				<Text style={styles.itemDescription}>{notification.description}</Text>
			</View>
		</View>
	);

	return notification.url ? <TouchableOpacity onPress={handlePress}>{content}</TouchableOpacity> : content;
};

const NotificationsPage = () => {
	const setUnseenCount = useAuthStore(state => state.setUnseenNotificationCount);
	const apiCall = useCallback(() => apiClient.get('/public/notifications'), []);
	const { data: notifications, loading, error } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	useEffect(() => {
		const markAsSeen = async () => {
			try {
				await apiClient.post('/public/notifications/mark-all-seen');
				setUnseenCount(0);
			} catch (err) { console.error("Failed to mark notifications as seen", err); }
		};
		markAsSeen();
	}, [setUnseenCount]);

	const unseenNotifications = notifications?.filter(n => !n.seen) || [];
	const seenNotifications = notifications?.filter(n => n.seen) || [];

	return (
		<View style={styles.container}>
			<Text style={styles.title}>Benachrichtigungen</Text>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={[...unseenNotifications, ...seenNotifications]}
                keyExtractor={item => item.id.toString()}
                renderItem={({item}) => <NotificationItem notification={item} styles={styles} colors={colors} />}
                ListHeaderComponent={
                    <Text style={styles.sectionHeader}>
                        {unseenNotifications.length > 0 ? 'Ungelesen' : 'Keine neuen Benachrichtigungen'}
                    </Text>
                }
            />
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        itemContainer: { flexDirection: 'row', alignItems: 'flex-start', padding: spacing.md, borderBottomWidth: 1, borderColor: colors.border },
        unseenItem: { backgroundColor: colors.primaryLight },
        itemTitle: { fontWeight: 'bold', fontSize: typography.body, color: colors.text, flex: 1 },
        itemDate: { fontSize: typography.caption, color: colors.textMuted },
        itemDescription: { marginTop: spacing.xs, color: colors.text },
        sectionHeader: { fontSize: typography.h4, fontWeight: 'bold', padding: spacing.md, backgroundColor: colors.background }
    });
};

export default NotificationsPage;