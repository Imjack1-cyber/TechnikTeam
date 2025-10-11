import React, { useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const PollsPage = () => {
    const navigation = useNavigation();

    const apiCall = useCallback(() => apiClient.get('/public/polls'), []);
    const { data: polls, loading, error } = useApi(apiCall, { subscribeTo: 'POLL' });

    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const { activePolls, closedPolls } = useMemo(() => {
        if (!polls) return { activePolls: [], closedPolls: [] };
        const active = polls.filter(p => !p.isClosed && (!p.closesAt || new Date(p.closesAt) > new Date()));
        const closed = polls.filter(p => p.isClosed || (p.closesAt && new Date(p.closesAt) <= new Date()));
        return { activePolls: active, closedPolls: closed };
    }, [polls]);

    const renderItem = ({ item }) => (
        <TouchableOpacity style={styles.card} onPress={() => navigation.navigate('PollDetails', { pollId: item.id })}>
            <Text style={styles.cardTitle}>{item.question}</Text>
            <View style={styles.metaContainer}>
                <Text style={styles.metaText}>Erstellt von {item.createdByUsername} am {new Date(item.createdAt).toLocaleDateString('de-DE')}</Text>
                {item.hasVoted && <Icon name="check-circle" solid size={16} color={colors.success} />}
            </View>
        </TouchableOpacity>
    );

    return (
        <View style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="poll" size={24} style={styles.headerIcon} />
                <Text style={styles.title}>Umfragen</Text>
            </View>

            {loading && <ActivityIndicator size="large" />}
            {error && <Text style={styles.errorText}>{error}</Text>}

            <FlatList
                data={activePolls}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                ListHeaderComponent={<Text style={styles.sectionHeader}>Aktive Umfragen</Text>}
                ListEmptyComponent={<Text style={styles.emptyText}>Keine aktiven Umfragen.</Text>}
            />

            <FlatList
                data={closedPolls}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                ListHeaderComponent={<Text style={styles.sectionHeader}>Abgeschlossene Umfragen</Text>}
                ListEmptyComponent={<Text style={styles.emptyText}>Keine abgeschlossenen Umfragen.</Text>}
            />
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1, backgroundColor: colors.background },
        headerContainer: { padding: spacing.md, flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: spacing.sm },
        sectionHeader: { fontSize: typography.h3, fontWeight: 'bold', color: colors.heading, paddingHorizontal: spacing.md, marginTop: spacing.md },
        metaContainer: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: spacing.sm },
        metaText: { fontSize: typography.caption, color: colors.textMuted },
        emptyText: { paddingHorizontal: spacing.md, color: colors.textMuted },
    });
};

export default PollsPage;