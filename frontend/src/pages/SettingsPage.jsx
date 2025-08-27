import React, { useState, useEffect, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Switch, Alert } from 'react-native';
import { useAuthStore } from '../store/authStore';
import { useToast } from '../context/ToastContext';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing, borders } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

// NOTE: Draggable list requires a library like 'react-native-draggable-flatlist'
// This implementation will use simple up/down arrows for reordering.

const SettingsPage = () => {
	const { layout, setLayout, navigationItems, isAdmin } = useAuthStore();
	const [sidebarPosition, setSidebarPosition] = useState(layout.sidebarPosition);
	const [navOrder, setNavOrder] = useState([]);
	const [showHelpButton, setShowHelpButton] = useState(layout.showHelpButton !== false);
	const [dashboardWidgets, setDashboardWidgets] = useState({ ...layout.dashboardWidgets });
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	useEffect(() => {
		const defaultOrder = navigationItems.map(item => item.label);
		const combinedOrder = [...(layout.navOrder || []), ...defaultOrder.filter(label => !(layout.navOrder || []).includes(label))];
		setNavOrder(combinedOrder);
	}, [layout.navOrder, navigationItems]);

	const handleSave = () => {
		const newLayout = { sidebarPosition, navOrder, showHelpButton, dashboardWidgets };
		setLayout(newLayout);
		addToast('Layout-Einstellungen gespeichert!', 'success');
	};

	const handleReset = () => {
        Alert.alert('Zurücksetzen', 'Möchten Sie alle Layout- und Navigationseinstellungen auf den Standard zurücksetzen?', [
            {text: 'Abbrechen', style: 'cancel'},
            {text: 'OK', onPress: () => {
                setLayout({ sidebarPosition: 'left', navOrder: [], showHelpButton: true, dashboardWidgets: {} });
                addToast('Einstellungen zurückgesetzt.', 'success');
            }}
        ]);
	};
    
	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<Text style={styles.title}>Layout-Einstellungen</Text>
			
            <View style={styles.card}>
                <Text style={styles.cardTitle}>Allgemein</Text>
                <View style={styles.switchRow}>
                    <Text style={styles.label}>Hilfe-Button anzeigen</Text>
                    <Switch value={showHelpButton} onValueChange={setShowHelpButton} />
                </View>
            </View>

            <View style={styles.card}>
                <Text style={styles.cardTitle}>Dashboard Widgets</Text>
                {Object.keys(dashboardWidgets).map(key => (
                     <View key={key} style={styles.switchRow}>
                        <Text style={styles.label}>{key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}</Text>
                        <Switch value={dashboardWidgets[key]} onValueChange={() => setDashboardWidgets(p => ({...p, [key]: !p[key]}))} />
                    </View>
                ))}
            </View>

			<View style={{flexDirection: 'row', justifyContent: 'space-between', marginTop: spacing.lg}}>
                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={handleReset}>
                    <Text style={styles.dangerOutlineButtonText}>Zurücksetzen</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleSave}>
                    <Text style={styles.buttonText}>Speichern</Text>
                </TouchableOpacity>
            </View>
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        switchRow: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            paddingVertical: spacing.sm,
            borderBottomWidth: 1,
            borderColor: colors.border,
        },
    });
};

export default SettingsPage;