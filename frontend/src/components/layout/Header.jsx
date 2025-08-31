import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Platform } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const Header = ({ navigation }) => {
	const user = useAuthStore(state => state.user);
	const unseenCount = user?.unseenNotificationsCount || 0;
    const insets = useSafeAreaInsets();

	return (
		<View style={[styles.header, { paddingTop: Platform.OS === 'ios' ? insets.top : 10, height: (Platform.OS === 'ios' ? insets.top : 10) + 54 }]}>
			<TouchableOpacity style={styles.toggleButton} onPress={() => navigation.toggleDrawer()}>
				<Icon name="bars" size={24} color="#212529" />
			</TouchableOpacity>
			<TouchableOpacity onPress={() => navigation.navigate('Dashboard')}>
				<Text style={styles.logo}>TechnikTeam</Text>
			</TouchableOpacity>
			<View style={styles.rightContainer}>
				<TouchableOpacity onPress={() => navigation.navigate('Benachrichtigungen')} style={styles.iconButton}>
					<Icon name="bell" solid size={24} color="#212529" />
					{unseenCount > 0 && (
						<View style={styles.badge}>
							<Text style={styles.badgeText}>{unseenCount}</Text>
						</View>
					)}
				</TouchableOpacity>
				<TouchableOpacity onPress={() => navigation.navigate('Profile')} style={styles.iconButton}>
					<Icon name={user?.profileIconClass?.replace('fa-', '') || 'user-circle'} solid size={24} color="#212529" />
				</TouchableOpacity>
			</View>
		</View>
	);
};

const styles = StyleSheet.create({
	header: {
		flexDirection: 'row',
		alignItems: 'center',
		justifyContent: 'space-between',
		backgroundColor: '#ffffff', // surface-color
		paddingHorizontal: 16,
		paddingBottom: 10,
		borderBottomWidth: 1,
		borderBottomColor: '#dee2e6', // border-color
	},
	toggleButton: {
		padding: 8,
	},
	logo: {
		fontWeight: '600',
		fontSize: 18,
		color: '#002B5B', // heading-color
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
		backgroundColor: '#dc3545', // danger-color
		borderRadius: 10,
		width: 20,
		height: 20,
		justifyContent: 'center',
		alignItems: 'center',
		borderWidth: 2,
		borderColor: '#ffffff', // surface-color
	},
	badgeText: {
		color: '#fff',
		fontSize: 10,
		fontWeight: 'bold',
	},
});

export default Header;