import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const AdminCoursesIndex = ({ navigation }) => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

	const baseLinks = [
		{ to: 'AdminCourses', label: 'Lehrgangs-Vorlagen', icon: 'book', perm: 'COURSE_READ' },
		{ to: 'AdminMatrix', label: 'Qualifikations-Matrix', icon: 'th-list', perm: 'QUALIFICATION_READ' },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<View style={styles.header}>
				<Icon name="graduation-cap" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Lehrgänge &amp; Skills</Text>
			</View>
			<Text style={styles.subtitle}>Verwalten Sie hier die Ausbildungs-Vorlagen und den Qualifikationsstatus der Mitglieder.</Text>

            <View style={styles.grid}>
                {baseLinks.filter(link => can(link.perm)).map(link => (
                    <TouchableOpacity key={link.to} style={styles.card} onPress={() => navigation.navigate(link.to)}>
                        <Icon name={link.icon} size={48} color={getThemeColors(theme).primary} />
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
            marginTop: 16,
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

export default AdminCoursesIndex;