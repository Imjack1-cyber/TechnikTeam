import React from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import Icon from '@expo/vector-icons/FontAwesome5';
import { getCommonStyles } from '../../styles/commonStyles';
import { useAuthStore } from '../../store/authStore';

const ProfileEventHistory = ({ eventHistory }) => {
	const navigation = useNavigation();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const formatDate = (dateString) => {
		if (!dateString) return '';
		return new Date(dateString).toLocaleString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
	};

	const renderItem = (item) => {
		const canGiveFeedback = item.status === 'ABGESCHLOSSEN' && item.userAttendanceStatus === 'ZUGEWIESEN';
		return (
			<View style={[styles.card, {marginBottom: 12}]} key={item.id}>
				<TouchableOpacity onPress={() => navigation.navigate('EventDetails', { eventId: item.id })}>
					<Text style={styles.cardTitle}>{item.name}</Text>
				</TouchableOpacity>
				<View style={styles.detailsListRow}>
					<Text style={styles.detailsListLabel}>Datum:</Text>
					<Text style={styles.detailsListValue}>{formatDate(item.eventDateTime)} Uhr</Text>
				</View>
				<View style={styles.detailsListRow}>
					<Text style={styles.detailsListLabel}>Dein Status:</Text>
					<Text style={styles.detailsListValue}>{item.userAttendanceStatus}</Text>
				</View>
				{canGiveFeedback && (
					<TouchableOpacity
						style={[styles.button, styles.primaryButton, {marginTop: 16}]}
						onPress={() => navigation.navigate('EventFeedback', { eventId: item.id })}
					>
						<Icon name="star" solid color="#fff" size={14} />
						<Text style={styles.buttonText}>Feedback geben</Text>
					</TouchableOpacity>
				)}
			</View>
		);
	};

	return (
		<View style={styles.card}>
			<Text style={styles.cardTitle}>Meine Event-Historie</Text>
			{eventHistory && eventHistory.length > 0 ? (
                // Using map instead of FlatList as it's not the main scroll view
                eventHistory.map(item => renderItem(item))
			) : (
                <Text>Keine Event-Historie vorhanden.</Text>
            )}
		</View>
	);
};

export default ProfileEventHistory;