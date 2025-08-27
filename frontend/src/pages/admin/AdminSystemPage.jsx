import React, { useCallback, useState, useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, TextInput, Alert } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import { RadioButton } from 'react-native-paper'; // Example for radio buttons

const MaintenanceModeManager = () => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	const apiCall = useCallback(() => apiClient.get('/admin/system/maintenance'), []);
	const { data, loading, error, reload } = useApi(apiCall);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [mode, setMode] = useState('OFF');
	const [message, setMessage] = useState('');
	const { addToast } = useToast();

	useEffect(() => {
		if (data) {
			setMode(data.mode || 'OFF');
			setMessage(data.message || '');
		}
	}, [data]);

	const handleSubmit = () => {
		const newStatus = { mode, message };
		const actionText = { OFF: 'deaktivieren', SOFT: 'aktivieren (Warnung)', HARD: 'aktivieren (Sperre)'}[mode];
        Alert.alert('Bestätigen', `Sind Sie sicher, dass Sie den Wartungsmodus ${actionText} möchten?`, [
            { text: 'Abbrechen', style: 'cancel'},
            { text: 'Ja', onPress: async () => {
                setIsSubmitting(true);
                try {
                    const result = await apiClient.post('/admin/system/maintenance', newStatus);
                    if (result.success) {
                        addToast(result.message, 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(err.message, 'error'); } finally { setIsSubmitting(false); }
            }}
        ]);
	};

	if (loading) return <ActivityIndicator />;
	if (error) return <Text style={styles.errorText}>{error}</Text>;

	return (
		<View style={styles.card}>
			<Text style={styles.cardTitle}>Wartungsmodus</Text>
			<Text style={styles.subtitle}>Steuern Sie den globalen Zugriffsstatus der Anwendung.</Text>
            
            <RadioButton.Group onValueChange={newValue => setMode(newValue)} value={mode}>
                <View style={styles.radioRow}><RadioButton value="OFF" /><Text>Aus</Text></View>
                <View style={styles.radioRow}><RadioButton value="SOFT" /><Text>Warnung (Banner)</Text></View>
                <View style={styles.radioRow}><RadioButton value="HARD" /><Text>Sperre (Nur Admins)</Text></View>
            </RadioButton.Group>
			
            <Text style={styles.label}>Angezeigte Nachricht</Text>
			<TextInput style={[styles.input, styles.textArea]} value={message} onChangeText={setMessage} multiline placeholder="z.B. Führen gerade Datenbank-Updates durch."/>
			
            <TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleSubmit} disabled={isSubmitting}>
				{isSubmitting ? <ActivityIndicator color="#fff"/> : <Text style={styles.buttonText}>Status aktualisieren</Text>}
			</TouchableOpacity>
		</View>
	);
};

const AdminSystemPage = () => {
	const apiCall = useCallback(() => apiClient.get('/system/stats'), []);
	const { data: stats, loading, error } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

	const formatPercent = (value) => `${value.toFixed(1)}%`;
	const formatGB = (value) => `${value.toFixed(2)} GB`;

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<View style={styles.headerContainer}>
                <Icon name="server" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Systeminformationen</Text>
            </View>
			<Text style={styles.subtitle}>Live-Statistiken über den Zustand des Servers.</Text>

			<MaintenanceModeManager />

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			{stats && (
				<>
                    <View style={styles.card}>
                        <Text style={styles.cardTitle}>CPU & Speicher</Text>
                        <View style={styles.detailRow}><Text style={styles.label}>CPU-Auslastung:</Text><Text style={styles.value}>{stats.cpuLoad > 0 ? formatPercent(stats.cpuLoad) : 'Wird geladen...'}</Text></View>
                        <View style={styles.detailRow}><Text style={styles.label}>RAM-Nutzung:</Text><Text style={styles.value}>{formatGB(stats.usedMemory)} / {formatGB(stats.totalMemory)}</Text></View>
                    </View>
                    <View style={styles.card}>
                        <Text style={styles.cardTitle}>Festplattenspeicher</Text>
                        <View style={styles.detailRow}><Text style={styles.label}>Speichernutzung:</Text><Text style={styles.value}>{formatGB(stats.usedDiskSpace)} / {formatGB(stats.totalDiskSpace)}</Text></View>
                    </View>
                    <View style={styles.card}>
                        <Text style={styles.cardTitle}>Laufzeit & Energie</Text>
                        <View style={styles.detailRow}><Text style={styles.label}>Server-Laufzeit:</Text><Text style={styles.value}>{stats.uptime}</Text></View>
                        <View style={styles.detailRow}><Text style={styles.label}>Batteriestatus:</Text><Text style={styles.value}>{stats.batteryPercentage >= 0 ? `${stats.batteryPercentage}%` : 'Nicht verfügbar'}</Text></View>
                    </View>
				</>
			)}
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        radioRow: { flexDirection: 'row', alignItems: 'center' },
    });
};

export default AdminSystemPage;