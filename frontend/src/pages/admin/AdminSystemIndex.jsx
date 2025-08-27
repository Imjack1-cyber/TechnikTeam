import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Linking } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const AdminSystemIndex = ({ navigation }) => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const baseLinks = [
		{ to: 'AdminSystemStatus', label: 'System-Status', icon: 'server', perm: 'SYSTEM_READ' },
		{ to: 'AdminAuthLog', label: 'Auth Log', icon: 'history', perm: 'LOG_READ' },
		{ to: 'AdminGeoIp', label: 'GeoIP Filter', icon: 'globe-americas', perm: 'ACCESS_ADMIN_PANEL' },
		{ to: 'AdminWiki', label: 'Technische Wiki', icon: 'book-reader', perm: 'ACCESS_ADMIN_PANEL' },
		{ to: 'http://10.0.2.2:8081/TechnikTeam/swagger-ui.html', label: 'API Docs (Swagger)', icon: 'code', perm: 'ACCESS_ADMIN_PANEL', isExternal: true },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};

    const handlePress = (link) => {
        if (link.isExternal) {
            Linking.openURL(link.to);
        } else {
            navigation.navigate(link.to);
        }
    };

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<View style={styles.header}>
				<Icon name="cogs" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>System &amp; Entwicklung</Text>
			</View>
			<Text style={styles.subtitle}>Technische Verwaltung, Dokumentation und Systemüberwachung.</Text>

            <View style={styles.grid}>
                {baseLinks.filter(link => can(link.perm)).map(link => (
                    <TouchableOpacity key={link.to} style={styles.card} onPress={() => handlePress(link)}>
                        <Icon name={link.icon} size={48} color={colors.primary} />
                        <Text style={styles.cardTitle}>{link.label}</Text>
                    </TouchableOpacity>
                ))}
            </View>
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        grid: {
            flexDirection: 'row',
            flexWrap: 'wrap',
            justifyContent: 'space-between',
        },
        card: {
            width: '48%',
            backgroundColor: colors.surface,
            borderRadius: 8,
            paddingVertical: 32,
            paddingHorizontal: 16,
            marginBottom: 16,
            alignItems: 'center',
            justifyContent: 'center',
            borderWidth: 1,
            borderColor: colors.border,
        },
        cardTitle: {
            fontSize: 16,
            fontWeight: 'bold',
            textAlign: 'center',
            color: colors.text,
            marginTop: 16,
        },
    });
}

export default AdminSystemIndex;