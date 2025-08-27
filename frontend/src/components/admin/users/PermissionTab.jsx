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
    
    // BouncyCheckbox doesn't support a container component well, so we wrap it
    const renderCheckbox = (p) => (
        <View key={p.id} style={styles.checkboxContainer}>
            <BouncyCheckbox
                size={25}
                fillColor={colors.primary}
                unfillColor="#FFFFFF"
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
			<Text style={styles.description}>Diese Berechtigungen gelten zusätzlich zu denen, die eine Rolle evtl. standardmäßig hat.</Text>
			<ScrollView style={styles.listContainer}>
				{Object.entries(groupedPermissions).map(([groupName, permissionsInGroup]) => (
                    // Accordion would be better here, using View for simplicity
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
        title: { fontSize: typography.h4, fontWeight: 'bold', marginBottom: spacing.sm },
        description: { color: colors.textMuted, marginBottom: spacing.md },
        listContainer: { maxHeight: '60%', borderWidth: 1, borderColor: colors.border, borderRadius: 8, padding: spacing.sm },
        groupName: { fontWeight: 'bold', fontSize: 16, paddingVertical: 8, marginTop: 8 },
        checkboxContainer: { flexDirection: 'row', alignItems: 'flex-start', marginBottom: 8 },
        permissionKey: { fontWeight: 'bold' },
        permissionDescription: { fontSize: typography.caption, color: colors.textMuted },
    });
};


export default PermissionsTab;