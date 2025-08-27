import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const Header = ({ onNavToggle }) => {
	const navigation = useNavigation();
	const user = useAuthStore(state => state.user);
	const unseenCount = user?.unseenNotificationsCount || 0;
    const insets = useSafeAreaInsets();

	return (
		<View style={[styles.header, { paddingTop: insets.top }]}>
			<TouchableOpacity onPress={onNavToggle} style={styles.toggleButton}>
				<Icon name="bars" size={24} color="#212529" />
			</TouchableOpacity>
			<TouchableOpacity onPress={() => navigation.navigate('Home')}>
				<Text style={styles.logo}>TechnikTeam</Text>
			</TouchableOpacity>
			<View style={styles.rightContainer}>
				<TouchableOpacity onPress={() => navigation.navigate('Notifications')} style={styles.iconButton}>
					<Icon name="bell" solid size={24} color="#212529" />
					{unseenCount > 0 && (
						<View style={styles.badge}>
							<Text style={styles.badgeText}>{unseenCount}</Text>
						</View>
					)}
				</TouchableOpacity>
				<TouchableOpacity onPress={() => navigation.navigate('Profile')} style={styles.iconButton}>
					<Icon name={user?.profileIconClass || 'user-circle'} solid size={24} color="#212529" />
				</TouchableOpacity>
			</View>
		</View>
	);
};

const styles = StyleSheet.create({
	header: {
		height: 64,
		flexDirection: 'row',
		alignItems: 'center',
		justifyContent: 'space-between',
		backgroundColor: '#ffffff', // surface-color
		paddingHorizontal: 16,
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