import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, TextInput, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import Icon from '@expo/vector-icons/FontAwesome5';

const TeamDirectoryPage = () => {
	const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/users'), []);
	const { data: users, loading, error } = useApi(apiCall, { subscribeTo: 'USER' });
	const [searchTerm, setSearchTerm] = useState('');

	const filteredUsers = users?.filter(user =>
		user.username.toLowerCase().includes(searchTerm.toLowerCase())
	);

	const renderUserCard = ({ item }) => (
		<View style={styles.card}>
			<Icon name={item.profileIconClass?.replace('fa-', '') || 'user-circle'} solid size={60} color="#6c757d" />
			<Text style={styles.userName}>{item.username}</Text>
			<Text style={styles.userRole}>{item.roleName}</Text>
			<TouchableOpacity
				style={styles.button}
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
				/>
			</View>

			{loading && <ActivityIndicator size="large" color="#007bff" style={{ marginTop: 20 }} />}
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

const styles = StyleSheet.create({
	container: { flex: 1, backgroundColor: '#f8f9fa' },
    header: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingTop: 16 },
    headerIcon: { color: '#002B5B', marginRight: 12 },
	title: { fontSize: 24, fontWeight: '700', color: '#002B5B' },
	description: { fontSize: 16, color: '#6c757d', paddingHorizontal: 16, marginVertical: 8 },
	searchCard: { backgroundColor: '#ffffff', borderRadius: 8, marginHorizontal: 16, padding: 8, borderWidth: 1, borderColor: '#dee2e6' },
	searchInput: { height: 40, fontSize: 16 },
	card: {
		flex: 1,
		backgroundColor: '#ffffff',
		borderRadius: 8,
		padding: 16,
		alignItems: 'center',
		borderWidth: 1,
		borderColor: '#dee2e6',
	},
	userName: {
		fontSize: 18,
		fontWeight: 'bold',
		marginTop: 16,
		marginBottom: 4,
	},
	userRole: {
		color: '#6c757d',
		marginBottom: 16,
	},
	button: {
		backgroundColor: '#007bff',
		paddingVertical: 8,
		paddingHorizontal: 16,
		borderRadius: 6,
	},
	buttonText: {
		color: '#fff',
		fontWeight: '500',
	},
	errorText: {
		color: '#dc3545',
		textAlign: 'center',
		marginTop: 20,
	},
});

export default TeamDirectoryPage;