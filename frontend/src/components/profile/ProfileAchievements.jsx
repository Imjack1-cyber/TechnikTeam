import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome5';

const ProfileAchievements = ({ achievements }) => {

	const renderItem = ({ item }) => (
		<View style={styles.card}>
			<Icon name={item.iconClass.replace('fa-', '')} size={48} color="#007bff" style={styles.icon} />
			<Text style={styles.name}>{item.name}</Text>
			<Text style={styles.description}>{item.description}</Text>
			<Text style={styles.earnedDate}>Verdient am: {new Date(item.earnedAt).toLocaleDateString('de-DE')}</Text>
		</View>
	);

	return (
		<View style={styles.container}>
			<Text style={styles.title}>Meine Abzeichen</Text>
			{achievements.length === 0 ? (
				<View style={[styles.card, styles.emptyCard]}>
					<Text>Du hast noch keine Abzeichen verdient. Nimm an Events teil, um sie freizuschalten!</Text>
				</View>
			) : (
				<FlatList
					data={achievements}
					renderItem={renderItem}
					keyExtractor={item => item.id.toString()}
					numColumns={2}
					columnWrapperStyle={{ gap: 12 }}
					contentContainerStyle={{ gap: 12, paddingHorizontal: 16 }}
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
		flex: 1,
		backgroundColor: '#ffffff',
		borderRadius: 8,
		padding: 16,
		alignItems: 'center',
		borderWidth: 1,
		borderColor: '#dee2e6',
	},
	emptyCard: {
		marginHorizontal: 16,
	},
	icon: {
		marginBottom: 16,
	},
	name: {
		fontSize: 16,
		fontWeight: 'bold',
		textAlign: 'center',
	},
	description: {
		color: '#6c757d',
		fontSize: 14,
		textAlign: 'center',
		marginVertical: 4,
	},
	earnedDate: {
		fontSize: 12,
		color: '#6c757d',
		marginTop: 8,
	},
});

export default ProfileAchievements;