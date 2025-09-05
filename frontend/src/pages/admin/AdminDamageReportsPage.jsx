import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, TextInput, ActivityIndicator, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography } from '../../styles/theme';

const ActionModal = ({ isOpen, onClose, onSuccess, report, action }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');
	const [notes, setNotes] = useState('');
	const [quantity, setQuantity] = useState('1');
	const { addToast } = useToast();

	const handleSubmit = async () => {
		setIsLoading(true);
		setError('');

		try {
			const payload = action === 'confirm' ? { quantity: parseInt(quantity, 10) } : { adminNotes: notes };
			const result = await apiClient.post(`/admin/damage-reports/${report.id}/${action}`, payload);

			if (result.success) {
				addToast(result.message, 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Aktion fehlgeschlagen');
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`Meldung #${report.id} ${action === 'confirm' ? 'bestätigen' : 'ablehnen'}`}>
			<View>
                <Text>Artikel: {report.itemName}</Text>
                <Text>Beschreibung des Nutzers: {report.reportDescription}</Text>
				{error && <Text style={styles.errorText}>{error}</Text>}
				{action === 'confirm' && (
					<View style={styles.formGroup}>
						<Text style={styles.label}>Anzahl als defekt markieren</Text>
						<TextInput style={styles.input} value={quantity} onChangeText={setQuantity} keyboardType="number-pad" />
					</View>
				)}
				{action === 'reject' && (
					<View style={styles.formGroup}>
						<Text style={styles.label}>Grund für die Ablehnung (optional)</Text>
						<TextInput style={[styles.input, styles.textArea]} value={notes} onChangeText={setNotes} multiline />
					</View>
				)}
				<View style={{flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16}}>
					<TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={onClose} disabled={isLoading}>
						<Text style={styles.buttonText}>Abbrechen</Text>
					</TouchableOpacity>
					<TouchableOpacity style={[styles.button, action === 'confirm' ? styles.successButton : styles.dangerButton]} onPress={handleSubmit} disabled={isLoading}>
						<Text style={styles.buttonText}>{action === 'confirm' ? 'Bestätigen' : 'Ablehnen'}</Text>
					</TouchableOpacity>
				</View>
			</View>
		</Modal>
	);
};

const AdminDamageReportsPage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/admin/damage-reports/pending'), []);
	const { data: reports, loading, error, reload } = useApi(apiCall);
	const [selectedReport, setSelectedReport] = useState(null);
	const [action, setAction] = useState(null);
    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };

	const openModal = (report, act) => {
		setSelectedReport(report);
		setAction(act);
	};

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <TouchableOpacity onPress={() => navigation.navigate('StorageItemDetails', { itemId: item.itemId })}>
                <Text style={styles.cardTitle}>{item.itemName}</Text>
            </TouchableOpacity>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Von:</Text>
                <Text style={styles.value} onPress={() => navigation.navigate('UserProfile', { userId: item.reporterUserId })}>{item.reporterUsername}</Text>
            </View>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Am:</Text>
                <Text style={styles.value}>{new Date(item.reportedAt).toLocaleString('de-DE')}</Text>
            </View>
            <Text style={styles.description}>Beschreibung: {item.reportDescription}</Text>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => openModal(item, 'confirm')}>
                    <Text style={styles.buttonText}>Bestätigen</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => openModal(item, 'reject')}>
                    <Text style={styles.buttonText}>Ablehnen</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="tools" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Offene Schadensmeldungen</Text>
            </View>
			<Text style={styles.subtitle}>Hier sehen Sie alle von Benutzern gemeldeten Schäden, die noch nicht von einem Admin bestätigt wurden.</Text>

			{loading && <ActivityIndicator size="large" style={{marginTop: 20}} />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={reports}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
                ListEmptyComponent={<View style={styles.card}><Text>Keine offenen Meldungen vorhanden.</Text></View>}
            />

			{selectedReport && (
				<ActionModal isOpen={!!selectedReport} onClose={() => setSelectedReport(null)} onSuccess={() => {setSelectedReport(null); reload();}} report={selectedReport} action={action} />
			)}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        description: { marginTop: 8, color: colors.text },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16 },
    });
};

export default AdminDamageReportsPage;