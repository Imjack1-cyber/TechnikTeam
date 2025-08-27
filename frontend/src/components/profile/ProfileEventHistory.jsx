import React from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import Icon from 'react-native-vector-icons/FontAwesome5';

const ProfileEventHistory = ({ eventHistory }) => {
	const navigation = useNavigation();

	const formatDate = (dateString) => {
		if (!dateString) return '';
		return new Date(dateString).toLocaleString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
	};

	const renderItem = ({ item }) => {
		const canGiveFeedback = item.status === 'ABGESCHLOSSEN' && item.userAttendanceStatus === 'ZUGEWIESEN';
		return (
			<View style={styles.card}>
				<TouchableOpacity onPress={() => navigation.navigate('EventDetails', { eventId: item.id })}>
					<Text style={styles.eventName}>{item.name}</Text>
				</TouchableOpacity>
				<View style={styles.row}>
					<Text style={styles.label}>Datum:</Text>
					<Text style={styles.value}>{formatDate(item.eventDateTime)} Uhr</Text>
				</View>
				<View style={styles.row}>
					<Text style={styles.label}>Dein Status:</Text>
					<Text style={styles.value}>{item.userAttendanceStatus}</Text>
				</View>
				{canGiveFeedback && (
					<TouchableOpacity
						style={styles.feedbackButton}
						onPress={() => navigation.navigate('EventFeedback', { eventId: item.id })}
					>
						<Icon name="star" solid color="#fff" size={14} />
						<Text style={styles.feedbackButtonText}>Feedback geben</Text>
					</TouchableOpacity>
				)}
			</View>
		);
	};

	return (
		<View style={styles.container}>
			<Text style={styles.title}>Meine Event-Historie</Text>
			{eventHistory.length === 0 ? (
				<View style={styles.card}>
					<Text>Keine Event-Historie vorhanden.</Text>
				</View>
			) : (
				<FlatList
					data={eventHistory}
					renderItem={renderItem}
					keyExtractor={item => item.id.toString()}
					contentContainerStyle={{ paddingBottom: 20 }}
				/>
			)}
		</View>
	);
};

const styles = StyleSheet.create({
	container: {
		flex: 1,
	},
	title: {
		fontSize: 20,
		fontWeight: '600',
		color: '#002B5B',
		marginBottom: 16,
		paddingHorizontal: 16,
	},
	card: {
		backgroundColor: '#ffffff',
		borderRadius: 8,
		padding: 16,
		marginHorizontal: 16,
		marginBottom: 12,
		borderWidth: 1,
		borderColor: '#dee2e6',
	},
	eventName: {
		fontSize: 16,
		fontWeight: 'bold',
		color: '#007bff',
		marginBottom: 8,
	},
	row: {
		flexDirection: 'row',
		justifyContent: 'space-between',
		paddingVertical: 4,
	},
	label: {
		color: '#6c757d',
		fontWeight: 'bold',
	},
	value: {
		color: '#212529',
	},
	feedbackButton: {
		flexDirection: 'row',
		alignItems: 'center',
		justifyContent: 'center',
		gap: 8,
		backgroundColor: '#007bff',
		paddingVertical: 8,
		borderRadius: 6,
		marginTop: 12,
	},
	feedbackButtonText: {
		color: '#fff',
		fontWeight: '500',
	},
});

export default ProfileEventHistory;