import React, { useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing } from '../styles/theme';
import BouncyCheckbox from "react-native-bouncy-checkbox";
import Icon from '@expo/vector-icons/FontAwesome5';

const PackKitPage = () => {
    const navigation = useNavigation();
	const route = useRoute();
	const { kitId } = route.params;
	const apiCall = useCallback(() => apiClient.get(`/public/kits/${kitId}`), [kitId]);
	const { data: kit, loading, error } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const handlePrint = () => {
        // Printing in React Native requires a dedicated library like 'react-native-print'.
        Alert.alert("Drucken", "Die Druckfunktion ist in der mobilen App nicht verfügbar.");
    };

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	if (!kit) return <View style={styles.centered}><Text style={styles.errorText}>Kit nicht gefunden.</Text></View>;

	return (
		<ScrollView style={styles.container}>
			<View style={styles.card}>
				<View style={styles.header}>
					<View style={{flex: 1}}>
						<Text style={styles.title}>Packliste: {kit.name}</Text>
						<Text style={styles.subtitle}>{kit.description}</Text>
					</View>
					<TouchableOpacity style={styles.printButton} onPress={handlePrint}>
						<Icon name="print" size={20} color={colors.text} />
					</TouchableOpacity>
				</View>

				{kit.location && (
					<View style={[styles.card, {backgroundColor: colors.background}]}>
						<Text style={styles.cardTitle}>Standort</Text>
						<Text style={styles.locationText}>{kit.location}</Text>
					</View>
				)}

				<Text style={styles.contentHeader}>Inhalt zum Einpacken</Text>
				{!kit.items || kit.items.length === 0 ? (
					<Text>Dieses Kit hat keinen definierten Inhalt.</Text>
				) : (
					kit.items.map(item => (
						<BouncyCheckbox
                            key={item.itemId}
                            style={{paddingVertical: 8}}
                            text={`${item.quantity}x ${item.itemName}`}
                            textStyle={{ color: colors.text, textDecorationLine: 'none', fontSize: 16 }}
                            fillColor={colors.primary}
                            innerIconStyle={{ borderWidth: 2 }}
                        />
					))
				)}
			</View>
			<TouchableOpacity style={[styles.button, styles.secondaryButton, {marginTop: 24}]} onPress={() => navigation.navigate('Lager')}>
				<Text style={styles.buttonText}>Zurück zur Lagerübersicht</Text>
			</TouchableOpacity>
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1, backgroundColor: colors.background, padding: spacing.md },
        header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
        printButton: { padding: spacing.sm },
        locationText: { fontSize: 18, fontWeight: '500' },
        contentHeader: { fontSize: 18, fontWeight: 'bold', marginTop: spacing.lg, marginBottom: spacing.sm },
    });
};

export default PackKitPage;