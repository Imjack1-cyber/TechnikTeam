import React, { useState, useMemo } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, TextInput, Linking } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import ThemeSwitcher from '../ui/ThemeSwitcher';
import Icon from 'react-native-vector-icons/FontAwesome5';

// This component is designed to be used as the `drawerContent` for a React Navigation Drawer.
const Sidebar = ({ navigation }) => {
	const { user, navigationItems, logout, layout } = useAuthStore();
	const [searchTerm, setSearchTerm] = useState('');

	const orderedNavItems = useMemo(() => {
		if (!navigationItems) return [];
		const userOrder = layout.navOrder || [];
		const itemMap = new Map(navigationItems.map(item => [item.label, item]));
		const ordered = userOrder.map(label => itemMap.get(label)).filter(Boolean);
		const remaining = navigationItems.filter(item => !userOrder.includes(item.label));
		return [...ordered, ...remaining];
	}, [navigationItems, layout.navOrder]);

	if (!user || !navigationItems) {
		return null;
	}

	const handleSearchSubmit = () => {
		if (searchTerm.trim()) {
			navigation.navigate('Search', { q: searchTerm.trim() });
			setSearchTerm('');
		}
	};

	const handleLogout = () => {
		navigation.closeDrawer();
		logout();
	};

	const userNavItems = orderedNavItems.filter(item => item.requiredPermission === null);
	const adminNavItems = orderedNavItems.filter(item => item.requiredPermission !== null);

	const renderNavItem = (item) => {
        const hasBadge = item.url === '/notifications' && user.unseenNotificationsCount > 0;
        
		if (item.url.startsWith('/swagger-ui')) {
			const swaggerUrl = `http://10.0.2.2:8081/TechnikTeam/swagger-ui.html`;
			return (
				<TouchableOpacity style={styles.navLink} onPress={() => Linking.openURL(swaggerUrl)}>
					<Icon name={item.icon} style={styles.navIcon} />
					<Text style={styles.navLabel}>{item.label}</Text>
				</TouchableOpacity>
			);
		}
		return (
			<TouchableOpacity style={styles.navLink} onPress={() => navigation.navigate(item.label)}>
				<Icon name={item.icon} style={styles.navIcon} />
				<Text style={styles.navLabel}>{item.label}</Text>
                {hasBadge && (
                    <View style={styles.badge}>
                        <Text style={styles.badgeText}>{user.unseenNotificationsCount}</Text>
                    </View>
                )}
			</TouchableOpacity>
		);
	};

	return (
		<View style={styles.container}>
			<View style={styles.header}>
				<Icon name="bolt" size={20} color="#007bff" />
				<Text style={styles.logo}>TechnikTeam</Text>
			</View>
			<View style={styles.searchContainer}>
				<Icon name="search" style={styles.searchIcon} />
				<TextInput
					style={styles.searchInput}
					placeholder="Suchen..."
					value={searchTerm}
					onChangeText={setSearchTerm}
					onSubmitEditing={handleSearchSubmit}
					returnKeyType="search"
				/>
			</View>
			<ScrollView style={styles.navScroller}>
				<Text style={styles.navSectionTitle}>Benutzerbereich</Text>
				{userNavItems.map(item => <View key={`${item.label}-${item.url}`}>{renderNavItem(item)}</View>)}

				{adminNavItems.length > 0 && <Text style={styles.navSectionTitle}>Admin-Bereich</Text>}
				{adminNavItems.map(item => <View key={`${item.label}-${item.url}`}>{renderNavItem(item)}</View>)}
			</ScrollView>
			<View style={styles.footer}>
				<Text style={styles.userInfo}>Angemeldet als: <Text style={{fontWeight: 'bold'}}>{user.username}</Text></Text>
				<View style={styles.footerActions}>
					<TouchableOpacity style={[styles.button, styles.secondaryButton, { flex: 1 }]} onPress={() => navigation.navigate('Profile')}>
						<Text style={styles.buttonTextSecondary}>Profil</Text>
					</TouchableOpacity>
					<TouchableOpacity style={[styles.button, styles.dangerButton, { flex: 1 }]} onPress={handleLogout}>
						<Text style={styles.buttonText}>Logout</Text>
					</TouchableOpacity>
					<ThemeSwitcher />
				</View>
			</View>
		</View>
	);
};

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: '#ffffff' },
    header: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 24, paddingVertical: 16, borderBottomWidth: 1, borderBottomColor: '#dee2e6' },
    logo: { fontSize: 18, fontWeight: '700', color: '#002B5B', marginLeft: 8 },
    searchContainer: { paddingHorizontal: 16, marginVertical: 8 },
    searchInput: { backgroundColor: '#f8f9fa', borderRadius: 20, paddingVertical: 8, paddingHorizontal: 16, paddingLeft: 40 },
    searchIcon: { position: 'absolute', top: 12, left: 30, color: '#6c757d' },
    navScroller: { flex: 1 },
    navSectionTitle: { paddingHorizontal: 24, paddingVertical: 8, marginTop: 8, fontSize: 12, fontWeight: '600', textTransform: 'uppercase', color: '#6c757d' },
    navLink: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, paddingHorizontal: 24, gap: 16 },
    navIcon: { fontSize: 18, color: '#6c757d', width: 24, textAlign: 'center' },
    navLabel: { fontSize: 16, fontWeight: '500', color: '#343a40' },
    badge: { backgroundColor: '#dc3545', borderRadius: 10, width: 20, height: 20, justifyContent: 'center', alignItems: 'center', marginLeft: 'auto' },
    badgeText: { color: '#fff', fontSize: 10, fontWeight: 'bold' },
    footer: { padding: 24, borderTopWidth: 1, borderTopColor: '#dee2e6' },
    userInfo: { fontSize: 14, color: '#6c757d', marginBottom: 16 },
    footerActions: { flexDirection: 'row', gap: 8, alignItems: 'center' },
    button: { paddingVertical: 8, paddingHorizontal: 12, borderRadius: 6 },
    secondaryButton: { backgroundColor: '#6c757d' },
    dangerButton: { backgroundColor: '#dc3545' },
    buttonText: { color: '#fff', textAlign: 'center' },
    buttonTextSecondary: { color: '#fff', textAlign: 'center' },
});

export default Sidebar;