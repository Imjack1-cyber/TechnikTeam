import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../../store/authStore';
import { getThemeColors, typography, spacing } from '../../../styles/theme';
import { getCommonStyles } from '../../../styles/commonStyles';

const Widget = ({ icon, title, children, linkTo, linkText }) => {
	const navigation = useNavigation();
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const commonStyles = getCommonStyles(theme);
    const styles = pageStyles(theme);

	return (
		<View style={commonStyles.card}>
			<View style={styles.titleContainer}>
				<Icon name={icon.replace('fa-', '')} size={18} color={colors.heading} />
				<Text style={styles.titleText}>{title}</Text>
			</View>
			{children}
			{linkTo && linkText && (
				<TouchableOpacity
					style={[commonStyles.button, { alignSelf: 'flex-start', marginTop: spacing.md, backgroundColor: colors.primaryLight }]}
					onPress={() => navigation.navigate(linkTo)}
				>
					<Text style={{ color: colors.primary, fontWeight: '500' }}>{linkText}</Text>
				</TouchableOpacity>
			)}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        titleContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            gap: spacing.sm,
            marginBottom: spacing.md,
            borderBottomWidth: 1,
            borderBottomColor: colors.border,
            paddingBottom: spacing.sm,
        },
        titleText: {
            fontSize: typography.h4,
            fontWeight: typography.fontWeights.bold,
            color: colors.heading,
        },
    });
};

export default Widget;