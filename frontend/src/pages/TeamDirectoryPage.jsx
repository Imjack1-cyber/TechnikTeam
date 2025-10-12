import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, TextInput, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing } from '../styles/theme';

const TeamDirectoryPage = () => {
    const navigation = useNavigation();
    const apiCall = useCallback(() => apiClient.get('/users'), []);
    const { data: users, loading, error } = useApi(apiCall, { subscribeTo: 'USER' });
    const [searchTerm, setSearchTerm] = useState('');
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const filteredUsers = users?.filter(user =>
        user.username.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const renderUserCard = ({ item }) => (
        <View style={styles.card}>
            <Icon name={item.profileIconClass?.replace('fa-', '') || 'user-circle'} solid size={60} color={colors.textMuted} />
            <Text style={styles.userName}>{item.username}</Text>
            <Text style={styles.userRole}>{item.roleName}</Text>
            <TouchableOpacity
                style={[styles.button, styles.primaryButton]}
                onPress={() => navigation.navigate('UserProfile', { userId: item.id })}
            >
                <Text style={styles.buttonText}>Profil ansehen</Text>
            </TouchableOpacity>
        </View>
    );

    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <Icon name="users" size={24} style={styles.headerIcon} />
                <Text style={styles.title}>Team-Verzeichnis</Text>
            </View>
            <Text style={styles.description}>Hier finden Sie eine Übersicht aller Mitglieder des Technik-Teams.</Text>

            <View style={styles.searchCard}>
                <TextInput
                    style={styles.searchInput}
                    value={searchTerm}
                    onChangeText={setSearchTerm}
                    placeholder="Mitglied suchen..."
                    placeholderTextColor={colors.textMuted}
                />
            </View>

            {loading && <ActivityIndicator size="large" color={colors.primary} style={{ marginTop: 20 }} />}
            {error && <Text style={styles.errorText}>{error}</Text>}

            <FlatList
                data={filteredUsers}
                renderItem={renderUserCard}
                keyExtractor={item => item.id.toString()}
                numColumns={2}
                columnWrapperStyle={{ gap: 16 }}
                contentContainerStyle={{ gap: 16, padding: 16 }}
            />
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: {
            flexDirection: 'row',
            alignItems: 'center',
            paddingHorizontal: 16,
            paddingTop: 16,
        },
        headerIcon: {
            color: colors.heading,
            marginRight: 12,
        },
        description: {
            fontSize: 16,
            color: colors.textMuted,
            paddingHorizontal: 16,
            marginBottom: 16,
        },
        searchCard: {
            backgroundColor: colors.surface,
            borderRadius: 8,
            marginHorizontal: 16,
            paddingHorizontal: 8,
            borderWidth: 1,
            borderColor: colors.border,
        },
        searchInput: {
            height: 40,
            fontSize: 16,
            color: colors.text,
        },
        userName: {
            fontSize: 18,
            fontWeight: 'bold',
            marginTop: 16,
            marginBottom: 4,
            color: colors.text,
        },
        userRole: {
            color: colors.textMuted,
            marginBottom: 16,
        },
    });
};

export default TeamDirectoryPage;