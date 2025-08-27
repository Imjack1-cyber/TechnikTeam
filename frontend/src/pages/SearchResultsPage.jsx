import React, { useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../styles/theme';

const SearchResultsPage = () => {
    const navigation = useNavigation();
	const route = useRoute();
	const query = route.params?.q || '';

	const apiCall = useCallback(() => {
		if (!query) return Promise.resolve({ success: true, data: [] });
		return apiClient.get(`/public/search?query=${encodeURIComponent(query)}`);
	}, [query]);

	const { data: results, loading, error } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const getIconForType = (type) => {
		switch (type) {
			case 'Veranstaltung': return 'calendar-check';
			case 'Lagerartikel': return 'cube';
			case 'Lehrgang': return 'graduation-cap';
			case 'Dokumentation': return 'file-alt';
			default: return 'search';
		}
	};
    
    const handlePress = (result) => {
        // This logic needs to be more robust based on the URL structure
        if (result.url.includes('/veranstaltungen/details/')) {
            navigation.navigate('EventDetails', { eventId: result.url.split('/').pop() });
        }
        // Add other navigation cases here
    };

    const renderItem = ({ item }) => (
        <TouchableOpacity style={styles.resultItem} onPress={() => handlePress(item)}>
            <Icon name={getIconForType(item.type)} size={24} color={colors.primary} />
            <View style={styles.resultDetails}>
                <Text style={styles.resultTitle}>{item.title}</Text>
                <Text style={styles.resultSnippet}>{item.snippet}</Text>
            </View>
            <Text style={styles.resultType}>{item.type}</Text>
        </TouchableOpacity>
    );

	return (
		<View style={styles.container}>
			<View style={styles.headerContainer}>
                <Icon name="search" size={24} style={styles.headerIcon}/>
                <Text style={styles.title}>Suchergebnisse</Text>
            </View>
			{query && <Text style={styles.subtitle}>Ergebnisse für: "{query}"</Text>}

			{loading && <ActivityIndicator size="large" style={{marginTop: 20}}/>}
			{error && <Text style={styles.errorText}>{error}</Text>}

			{results && (
                <FlatList
                    data={results}
                    renderItem={renderItem}
                    keyExtractor={(item, index) => `${item.url}-${index}`}
                    contentContainerStyle={styles.contentContainer}
                    ListEmptyComponent={!loading ? <Text style={styles.emptyText}>Keine Ergebnisse gefunden.</Text> : null}
                />
			)}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        resultItem: { flexDirection: 'row', alignItems: 'center', padding: spacing.md, borderBottomWidth: 1, borderColor: colors.border, gap: spacing.md },
        resultDetails: { flex: 1 },
        resultTitle: { fontWeight: 'bold', fontSize: typography.body, color: colors.primary },
        resultSnippet: { fontSize: typography.small, color: colors.textMuted, marginTop: 4 },
        resultType: { fontSize: typography.caption, color: colors.textMuted, alignSelf: 'flex-start' },
        emptyText: { textAlign: 'center', marginTop: 20, color: colors.textMuted },
    });
};

export default SearchResultsPage;