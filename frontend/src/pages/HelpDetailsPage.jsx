import React, { useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, Linking } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import MarkdownDisplay from 'react-native-markdown-display';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const HelpDetailsPage = () => {
    const navigation = useNavigation();
	const route = useRoute();
	const { pageKey } = route.params;
	const apiCall = useCallback(() => apiClient.get(`/public/documentation/${pageKey}`), [pageKey]);
	const { data: doc, loading, error } = useApi(apiCall);
	const { data: allDocs } = useApi(useCallback(() => apiClient.get('/public/documentation'), []));
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const relatedPageDetails = useMemo(() => {
		if (!doc?.relatedPages || !allDocs) return [];
		try {
			const relatedKeys = JSON.parse(doc.relatedPages);
			return relatedKeys.map(key => allDocs.find(d => d.pageKey === key)).filter(Boolean);
		} catch (e) { return []; }
	}, [doc, allDocs]);

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large"/></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	if (!doc) return <View style={styles.centered}><Text>Dokumentation nicht gefunden.</Text></View>;

    const handleOpenPage = () => {
        // This is a simplified example. A robust solution would map pagePath to navigation routes.
        const routeName = doc.pagePath.split('/')[1]; // e.g., /home -> home
        if (routeName) {
            const capitalizedRoute = routeName.charAt(0).toUpperCase() + routeName.slice(1);
            if (navigation.getState().routeNames.includes(capitalizedRoute)) {
                navigation.navigate(capitalizedRoute);
            } else {
                 addToast("Navigation zu dieser Seite wird noch nicht unterstützt.", "info");
            }
        }
    };
    
	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<View style={styles.header}>
				<Text style={styles.title}>{doc.title}</Text>
				<TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={handleOpenPage}>
					<Icon name="external-link-alt" size={14} color={colors.text} />
                    <Text style={{color: colors.text}}>Seite öffnen</Text>
				</TouchableOpacity>
			</View>

			<View style={styles.card}>
				<Text style={styles.cardTitle}>Features</Text>
				<MarkdownDisplay>{doc.features}</MarkdownDisplay>
			</View>
            
            <View style={styles.card}>
                <Text style={styles.cardTitle}>Verknüpfte Seiten</Text>
                {relatedPageDetails.length > 0 ? (
                    relatedPageDetails.map(p => (
                        <TouchableOpacity key={p.id} style={styles.detailsListRow} onPress={() => navigation.push('HelpDetails', { pageKey: p.pageKey })}>
                            <Text style={styles.linkText}>{p.title}</Text>
                        </TouchableOpacity>
                    ))
                ) : <Text>Keine verknüpften Seiten.</Text>}
            </View>
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: spacing.md },
        linkText: { color: colors.primary, fontSize: typography.body },
    });
};

export default HelpDetailsPage;