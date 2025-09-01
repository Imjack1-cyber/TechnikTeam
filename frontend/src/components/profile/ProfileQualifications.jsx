import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';
import { getCommonStyles } from '../../styles/commonStyles';
import { useAuthStore } from '../../store/authStore';

const ProfileQualifications = ({ qualifications }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const renderItem = ({ item }) => (
		<View style={styles.detailsListRow}>
			<Text style={styles.detailsListLabel}>{item.courseName}</Text>
			<Text style={styles.detailsListValue}>{item.status}</Text>
		</View>
	);

	return (
		<View style={styles.card}>
			<Text style={styles.cardTitle}>Meine Qualifikationen</Text>
			{!qualifications || qualifications.length === 0 ? (
				<Text>Keine Qualifikationen erworben.</Text>
			) : (
				<FlatList
					data={qualifications}
					renderItem={renderItem}
					keyExtractor={item => item.courseId.toString()}
				/>
			)}
		</View>
	);
};

export default ProfileQualifications;