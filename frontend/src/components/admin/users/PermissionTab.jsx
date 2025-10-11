import React from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator } from 'react-native';
import { useAuthStore } from '../../../store/authStore';
import { getThemeColors, typography, spacing } from '../../../styles/theme';
import BouncyCheckbox from "react-native-bouncy-checkbox";

const PermissionsTab = ({ groupedPermissions, assignedIds, onPermissionChange, isLoading }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);

	if (isLoading) {
		return <ActivityIndicator />;
	}
    
    const renderCheckbox = (p) => (
        <View key={p.id} style={styles.checkboxContainer}>
            <BouncyCheckbox
                size={25}
                fillColor={colors.primary}
                unfillColor={colors.surface}
                iconStyle={{ borderColor: colors.border }}
                innerIconStyle={{ borderWidth: 2 }}
                isChecked={assignedIds.has(p.id)}
                onPress={() => onPermissionChange(p.id)}
            />
            <View style={{flex: 1}}>
                <Text style={styles.permissionKey}>{p.permissionKey.replace(p.groupName + '_', '')}</Text>
                <Text style={styles.permissionDescription}>{p.description}</Text>
            </View>
        </View>
    );

	return (
		<View>
			<Text style={styles.title}>Individuelle Berechtigungen</Text>
			<Text style={styles.description}>Weisen Sie einem Benutzer individuelle Berechtigungen zu, die seine Rollenberechtigungen überschreiben.</Text>
			<View style={styles.listContainer}>
				{Object.entries(groupedPermissions).map(([groupName, permissionsInGroup]) => (
					<View key={groupName}>
						<Text style={styles.groupName}>{groupName}</Text>
						<View style={{ paddingLeft: 16 }}>
							{permissionsInGroup.map(p => renderCheckbox({...p, groupName}))}
						</View>
					</View>
				))}
			</View>
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        title: { fontSize: typography.h4, fontWeight: 'bold', marginBottom: spacing.sm, color: colors.heading },
        description: { color: colors.textMuted, marginBottom: spacing.md, fontStyle: 'italic' },
        listContainer: { borderWidth: 1, borderColor: colors.border, borderRadius: 8, padding: spacing.sm },
        groupName: { fontWeight: 'bold', fontSize: 16, paddingVertical: 8, marginTop: 8, color: colors.text },
        checkboxContainer: { flexDirection: 'row', alignItems: 'flex-start', marginBottom: 2, paddingVertical: 0 },
        permissionKey: { fontWeight: 'bold', color: colors.text },
        permissionDescription: { fontSize: typography.caption, color: colors.textMuted },
    });
};


export default PermissionsTab;