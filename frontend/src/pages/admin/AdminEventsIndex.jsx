import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors } from '../../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const AdminEventsIndex = ({ navigation }) => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const baseLinks = [
		{ to: 'AdminEvents', label: 'Events Verwalten', icon: 'calendar-plus', perm: 'EVENT_READ' },
		{ to: 'AdminDebriefingsList', label: 'Debriefing-Übersicht', icon: 'clipboard-check', perm: 'EVENT_DEBRIEFING_VIEW' },
		// { to: 'AdminEventRoles', label: 'Event-Rollen', icon: 'user-tag', perm: 'EVENT_CREATE' }, // TODO: Re-enable when AdminEventRolesPage.jsx is created/fixed
		{ to: 'AdminVenues', label: 'Veranstaltungsorte', icon: 'map-marked-alt', perm: 'EVENT_CREATE' },
		{ to: 'AdminChecklistTemplates', label: 'Checklist-Vorlagen', icon: 'tasks', perm: 'EVENT_MANAGE_TASKS' },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<View style={styles.header}>
				<Icon name="calendar-alt" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Event Management</Text>
			</View>
			<Text style={styles.subtitle}>Zentrale Anlaufstelle für die Planung und Verwaltung aller Veranstaltungen.</Text>

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
            width: '48%', // Two columns
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

export default AdminEventsIndex;