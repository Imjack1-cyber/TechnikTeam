import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';

const ProfileAchievements = ({ achievements }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const renderItem = ({ item }) => (
		<View style={styles.achievementCard}>
			<Icon name={item.iconClass.replace('fa-', '')} size={48} color={colors.primary} style={styles.icon} />
			<Text style={styles.name}>{item.name}</Text>
			<Text style={styles.description}>{item.description}</Text>
			<Text style={styles.earnedDate}>Verdient am: {new Date(item.earnedAt).toLocaleDateString('de-DE')}</Text>
		</View>
	);

	return (
		<View style={styles.card}>
			<Text style={styles.cardTitle}>Meine Abzeichen</Text>
			{!achievements || achievements.length === 0 ? (
				<Text style={styles.bodyText}>Du hast noch keine Abzeichen verdient. Nimm an Events teil, um sie freizuschalten!</Text>
			) : (
				<FlatList
					data={achievements}
					renderItem={renderItem}
					keyExtractor={item => item.id.toString()}
                    horizontal
                    showsHorizontalScrollIndicator={false}
				/>
			)}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        achievementCard: {
            backgroundColor: colors.surface,
            borderRadius: 8,
            padding: spacing.md,
            alignItems: 'center',
            borderWidth: 1,
            borderColor: colors.border,
            width: 200,
            marginRight: spacing.md,
        },
        icon: {
            marginBottom: spacing.md,
        },
        name: {
            fontSize: typography.body,
            fontWeight: 'bold',
            textAlign: 'center',
            color: colors.text,
        },
        description: {
            color: colors.textMuted,
            fontSize: typography.small,
            textAlign: 'center',
            marginVertical: 4,
            minHeight: 40,
        },
        earnedDate: {
            fontSize: typography.caption,
            color: colors.textMuted,
            marginTop: 8,
        },
    });
};

export default ProfileAchievements;