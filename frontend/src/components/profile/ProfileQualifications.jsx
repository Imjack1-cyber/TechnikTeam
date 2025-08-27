import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';

const ProfileQualifications = ({ qualifications }) => {

	const renderItem = ({ item }) => (
		<View style={styles.row}>
			<Text style={styles.courseName}>{item.courseName}</Text>
			<Text style={styles.status}>{item.status}</Text>
		</View>
	);

	return (
		<View style={styles.card}>
			<Text style={styles.title}>Meine Qualifikationen</Text>
			{qualifications.length === 0 ? (
				<Text>Keine Qualifikationen erworben.</Text>
			) : (
				<FlatList
					data={qualifications}
					renderItem={renderItem}
					keyExtractor={item => item.courseId.toString()}
					ListHeaderComponent={() => (
						<View style={[styles.row, styles.headerRow]}>
							<Text style={styles.headerText}>Lehrgang</Text>
							<Text style={styles.headerText}>Status</Text>
						</View>
					)}
				/>
			)}
		</View>
	);
};

const styles = StyleSheet.create({
	card: {
		backgroundColor: '#ffffff',
		borderRadius: 8,
		padding: 16,
		marginHorizontal: 16,
		marginBottom: 16,
		borderWidth: 1,
		borderColor: '#dee2e6',
	},
	title: {
		fontSize: 20,
		fontWeight: '600',
		color: '#002B5B',
		marginBottom: 12,
	},
	headerRow: {
		borderBottomWidth: 2,
		borderBottomColor: '#dee2e6',
	},
	headerText: {
		fontWeight: 'bold',
		color: '#6c757d',
		flex: 1,
	},
	row: {
		flexDirection: 'row',
		justifyContent: 'space-between',
		paddingVertical: 10,
		borderBottomWidth: 1,
		borderBottomColor: '#f0f0f0',
	},
	courseName: {
		flex: 1,
		color: '#212529',
	},
	status: {
		flex: 1,
		textAlign: 'right',
		color: '#212529',
	},
});

export default ProfileQualifications;