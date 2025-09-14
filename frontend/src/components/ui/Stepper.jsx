import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, spacing, typography } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const Stepper = ({ steps, currentStep }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);

    return (
        <View style={styles.container}>
            {steps.map((step, index) => {
                const isActive = index === currentStep;
                const isCompleted = index < currentStep;

                return (
                    <React.Fragment key={index}>
                        <View style={styles.stepContainer}>
                            <View style={[styles.dot, isActive && styles.activeDot, isCompleted && styles.completedDot]}>
                                {isCompleted ? (
                                    <Icon name="check" size={12} color={colors.white} />
                                ) : (
                                    <Text style={[styles.dotText, isActive && styles.activeDotText]}>{index + 1}</Text>
                                )}
                            </View>
                            <Text style={[styles.stepLabel, (isActive || isCompleted) && styles.activeStepLabel]} numberOfLines={1}>
                                {step}
                            </Text>
                        </View>
                        {index < steps.length - 1 && (
                            <View style={[styles.line, isCompleted && styles.completedLine]} />
                        )}
                    </React.Fragment>
                );
            })}
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: {
            flexDirection: 'row',
            alignItems: 'flex-start',
            justifyContent: 'space-between',
            paddingHorizontal: spacing.sm,
            paddingVertical: spacing.md,
        },
        stepContainer: {
            alignItems: 'center',
            maxWidth: 80, // Prevent labels from overlapping
        },
        dot: {
            width: 30,
            height: 30,
            borderRadius: 15,
            backgroundColor: colors.surface,
            borderWidth: 2,
            borderColor: colors.border,
            justifyContent: 'center',
            alignItems: 'center',
        },
        activeDot: {
            borderColor: colors.primary,
        },
        completedDot: {
            backgroundColor: colors.primary,
            borderColor: colors.primary,
        },
        dotText: {
            color: colors.textMuted,
            fontWeight: 'bold',
        },
        activeDotText: {
            color: colors.primary,
        },
        stepLabel: {
            marginTop: spacing.xs,
            fontSize: typography.caption,
            color: colors.textMuted,
            textAlign: 'center',
        },
        activeStepLabel: {
            color: colors.primary,
            fontWeight: 'bold',
        },
        line: {
            flex: 1,
            height: 2,
            backgroundColor: colors.border,
            marginTop: 14, // Align with center of the dot
        },
        completedLine: {
            backgroundColor: colors.primary,
        },
    });
};

export default Stepper;