import React from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator } from 'react-native';
import { useAuthStore } from '../../../store/authStore';
import { getThemeColors, typography, spacing } from '../../../styles/theme';
import BouncyCheckbox from "react-native-bouncy-checkbox";

const PermissionsTab = ({ groupedPermissions, assignedIds, inheritedIds, onPermissionChange, isLoading }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);

	if (isLoading) {
		return <ActivityIndicator />;
	}
    
    const renderCheckbox = (p) => {
        const isDirect = assignedIds.has(p.id);
        const isInherited = !isDirect && inheritedIds.has(p.id);

        return (
            <View key={p.id} style={styles.checkboxContainer}>
                <BouncyCheckbox
                    size={25}
                    fillColor={isInherited ? colors.textMuted : colors.primary}
                    unfillColor={colors.surface}
                    iconStyle={{ borderColor: colors.border }}
                    innerIconStyle={{ borderWidth: 2 }}
                    isChecked={isDirect || isInherited}
                    onPress={() => onPermissionChange(p.id)}
                    disableBuiltInState
                    disabled={isInherited}
                />
                <View style={{flex: 1}}>
                    <Text style={[styles.permissionKey, isInherited && {color: colors.textMuted}]}>{p.permissionKey.replace(p.groupName + '_', '')}</Text>
                    <Text style={[styles.permissionDescription, isInherited && {color: colors.textMuted}]}>{p.description}</Text>
                </View>
            </View>
        );
    };

	return (
		<View style={{ flex: 1 }}>
			<Text style={styles.title}>Individuelle Berechtigungen</Text>
			<Text style={styles.description}>Weisen Sie einem Benutzer individuelle Berechtigungen zu. Grau markierte Berechtigungen werden von der Rolle geerbt.</Text>
			<ScrollView style={styles.listContainer}>
				{Object.entries(groupedPermissions).map(([groupName, permissionsInGroup]) => (
					<View key={groupName}>
						<Text style={styles.groupName}>{groupName}</Text>
						<View style={{ paddingLeft: 16 }}>
							{permissionsInGroup.map(p => renderCheckbox({...p, groupName}))}
						</View>
					</View>
				))}
			</ScrollView>
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        title: { fontSize: typography.h4, fontWeight: 'bold', marginBottom: spacing.sm, color: colors.heading },
        description: { color: colors.textMuted, marginBottom: spacing.md, fontStyle: 'italic' },
        listContainer: { 
            borderWidth: 1, 
            borderColor: colors.border, 
            borderRadius: 8, 
            padding: spacing.sm,
            flex: 1, // Allow ScrollView to expand within its flex container
        },
        groupName: { fontWeight: 'bold', fontSize: 16, paddingVertical: 8, marginTop: 8, color: colors.text },
        checkboxContainer: { flexDirection: 'row', alignItems: 'flex-start', marginBottom: 2, paddingVertical: 0 },
        permissionKey: { fontWeight: 'bold', color: colors.text },
        permissionDescription: { fontSize: typography.caption, color: colors.textMuted },
    });
};


export default PermissionsTab;