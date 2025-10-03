import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Platform } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { DrawerActions } from '@react-navigation/native';
import { getThemeColors, spacing } from '../../styles/theme';

const Header = ({ navigation, route, options, back }) => {
	const { user, theme } = useAuthStore(state => ({
        user: state.user,
        theme: state.theme
    }));
	const unseenCount = user?.unseenNotificationsCount || 0;
    const insets = useSafeAreaInsets();
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);

    const title = options?.title ?? route.name;

	return (
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