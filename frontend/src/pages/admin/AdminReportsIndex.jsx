import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const AdminReportsIndex = ({ navigation }) => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const baseLinks = [
		{ to: 'AdminReports', label: 'Berichte & Analysen', icon: 'chart-pie', perm: 'REPORT_READ' },
		{ to: 'AdminLog', label: 'Aktions-Log', icon: 'clipboard-list', perm: 'LOG_READ' },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<View style={styles.header}>
				<Icon name="chart-line" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Berichte &amp; Logs</Text>
			</View>
			<Text style={styles.subtitle}>Analysieren Sie die Anwendungsnutzung und überwachen Sie Systemaktivitäten.</Text>

            <View style={styles.grid}>
                {baseLinks.filter(link => can(link.perm)).map(link => (
                    <TouchableOpacity key={link.to} style={styles.card} onPress={() => navigation.navigate(link.to)}>
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

export default AdminReportsIndex;