import React, { useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator } from 'react-native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from '@expo/vector-icons/FontAwesome5';

const AnnouncementsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/announcements'), []);
	const { data: announcements, loading, error } = useApi(apiCall);

	const renderContent = () => {
		if (loading) {
			return <ActivityIndicator size="large" color="#007bff" />;
		}
		if (error) {
			return <Text style={styles.errorText}>{error}</Text>;
		}
		if (announcements?.length === 0) {
			return (
				<View style={styles.card}>
					<Text>Aktuell gibt es keine neuen Mitteilungen.</Text>
				</View>
			);
		}
		return announcements?.map(post => (
			<View style={styles.card} key={post.id}>
				<Text style={styles.cardTitle}>{post.title}</Text>
				<Text style={styles.subtitle}>
					Gepostet von <Text style={{ fontWeight: 'bold' }}>{post.authorUsername}</Text> am {new Date(post.createdAt).toLocaleDateString('de-DE')}
				</Text>
				<View style={styles.markdownContent}>
					<MarkdownDisplay>{post.content}</MarkdownDisplay>
				</View>
			</View>
		));
	};

	return (
		<ScrollView style={styles.container}>
			<View style={styles.header}>
				<Icon name="thumbtack" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Anschlagbrett</Text>
			</View>
			<Text style={styles.description}>Wichtige und langfristige Mitteilungen für das gesamte Team.</Text>
			{renderContent()}
		</ScrollView>
	);
};

const styles = StyleSheet.create({
	container: {
		flex: 1,
		backgroundColor: '#f8f9fa',
	},
	header: {
		flexDirection: 'row',
		alignItems: 'center',
		padding: 16,
	},
	headerIcon: {
		color: '#002B5B',
		marginRight: 12,
	},
	title: {
		fontSize: 24,
		fontWeight: '700',
		color: '#002B5B',
	},
	description: {
		fontSize: 16,
		color: '#6c757d',
		paddingHorizontal: 16,
		marginBottom: 16,
	},
	card: {
		backgroundColor: '#ffffff',
		borderRadius: 8,
		padding: 16,
		marginHorizontal: 16,
		marginBottom: 16,
		borderWidth: 1,
		borderColor: '#dee2e6',
	},
	cardTitle: {
		fontSize: 18,
		fontWeight: '600',
		color: '#002B5B',
	},
	subtitle: {
		color: '#6c757d',
		marginTop: 4,
		marginBottom: 12,
		fontSize: 12,
	},
	markdownContent: {
		// Styles for Markdown can be passed to the MarkdownDisplay component
	},
	errorText: {
		color: '#dc3545',
		padding: 16,
		textAlign: 'center',
	}
});


export default AnnouncementsPage;