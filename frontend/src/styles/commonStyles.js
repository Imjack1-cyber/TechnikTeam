import { StyleSheet } from 'react-native';
import { getThemeColors, typography, spacing, borders, shadows } from './theme';

export const getCommonStyles = (theme) => {
	const colors = getThemeColors(theme);

	return StyleSheet.create({
		// Layout & Containers
		container: {
			flex: 1,
			backgroundColor: colors.background,
		},
		contentContainer: {
			padding: spacing.md,
		},
		card: {
			backgroundColor: colors.surface,
			borderRadius: borders.radius,
			padding: spacing.md,
			marginBottom: spacing.md,
			borderWidth: borders.width,
			borderColor: colors.border,
			...shadows.sm,
		},
		centered: {
			flex: 1,
			justifyContent: 'center',
			alignItems: 'center',
		},

		// Typography
		title: {
			fontSize: typography.h2,
			fontWeight: typography.fontWeights.bold,
			color: colors.heading,
			marginBottom: spacing.md,
		},
		subtitle: {
			fontSize: typography.body,
			color: colors.textMuted,
			marginBottom: spacing.md,
		},
		bodyText: {
			fontSize: typography.body,
			color: colors.text,
			lineHeight: typography.body * 1.5,
		},
		errorText: {
			color: colors.danger,
			textAlign: 'center',
			marginVertical: spacing.sm,
		},
		infoMessage: {
			backgroundColor: colors.primaryLight,
			padding: spacing.md,
			borderRadius: borders.radius,
			color: colors.primary,
		},

		// Buttons
		button: {
			paddingVertical: 12,
			paddingHorizontal: 20,
			borderRadius: borders.radius,
			alignItems: 'center',
			justifyContent: 'center',
			flexDirection: 'row',
			gap: spacing.sm,
		},
		buttonText: {
			color: colors.white,
			fontWeight: typography.fontWeights.medium,
			fontSize: typography.body,
		},
		primaryButton: {
			backgroundColor: colors.primary,
		},
		successButton: {
			backgroundColor: colors.success,
		},
		dangerButton: {
			backgroundColor: colors.danger,
		},
		secondaryButton: {
			backgroundColor: colors.textMuted,
		},
		outlineButton: {
			backgroundColor: 'transparent',
			borderWidth: borders.width,
		},
		dangerOutlineButton: {
			borderColor: colors.danger,
		},
		dangerOutlineButtonText: {
			color: colors.danger,
		},
        disabledButton: {
            opacity: 0.65,
        },

		// Forms
		formGroup: {
			marginBottom: spacing.md,
		},
		label: {
			fontSize: typography.small,
			fontWeight: typography.fontWeights.medium,
			color: colors.textMuted,
			marginBottom: spacing.sm,
		},
		input: {
			height: 48,
			borderWidth: borders.width,
			borderColor: colors.border,
			borderRadius: borders.radius,
			paddingHorizontal: spacing.md,
			backgroundColor: colors.surface,
			color: colors.text,
			fontSize: typography.body,
		},
        textArea: {
            minHeight: 120,
            textAlignVertical: 'top',
        },
        
        // Lists
        detailsList: {
            flexDirection: 'column',
        },
        detailsListRow: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            paddingVertical: spacing.md,
            borderBottomWidth: borders.width,
            borderBottomColor: colors.border,
        },
        detailsListLabel: {
            fontWeight: typography.fontWeights.bold,
            color: colors.text,
        },
        detailsListValue: {
            color: colors.textMuted,
        },
	});
};