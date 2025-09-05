import React, { useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, SectionList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const HelpListPage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/public/documentation'), []);
	const { data: docs, loading, error } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const sections = useMemo(() => {
		if (!docs) return [];
		const grouped = docs.reduce((acc, doc) => {
			const category = doc.category || 'Sonstiges';
			if (!acc[category]) acc[category] = [];
			acc[category].push(doc);
			return acc;
		}, {});
        return Object.entries(grouped).map(([title, data]) => ({ title, data }));
	}, [docs]);

    const renderItem = ({ item }) => (
        <TouchableOpacity style={styles.itemContainer} onPress={() => navigation.navigate('HelpDetails', { pageKey: item.pageKey })}>
            {item.adminOnly && <Icon name="user-shield" size={16} style={styles.adminIcon} />}
            <Text style={styles.itemTitle}>{item.title}</Text>
            <Icon name="chevron-right" size={16} color={colors.textMuted} />
        </TouchableOpacity>
    );

    const renderSectionHeader = ({ section: { title } }) => (
        <Text style={styles.sectionHeader}>{title}</Text>
    );

	return (
		<View style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="question-circle" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Hilfe & Dokumentation</Text>
            </View>
			<Text style={styles.subtitle}>Hier finden Sie Erklärungen zu den einzelnen Seiten und Funktionen der Anwendung.</Text>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<SectionList
                sections={sections}
                keyExtractor={(item) => item.id.toString()}
                renderItem={renderItem}
                renderSectionHeader={renderSectionHeader}
                contentContainerStyle={styles.contentContainer}
            />
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        sectionHeader: { 
            fontSize: typography.h3, 
            fontWeight: 'bold', 
            color: colors.heading, 
            marginTop: spacing.lg, 
            marginBottom: spacing.sm,
            paddingHorizontal: spacing.md,
        },
        itemContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            padding: spacing.md,
            backgroundColor: colors.surface,
            borderBottomWidth: 1,
            borderColor: colors.border,
        },
        adminIcon: {
            marginRight: spacing.sm,
            color: colors.primary,
        },
        itemTitle: {
            flex: 1,
            fontSize: typography.body,
            color: colors.text,
        },
    });
};

export default HelpListPage;