import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../ui/Modal';
import Icon from 'react-native-vector-icons/FontAwesome5';

const ConfirmationModal = ({ isOpen, onClose, onConfirm, changes, isSubmitting }) => {
	if (!isOpen) return null;

	const changeLabels = {
		email: 'E-Mail',
		classYear: 'Jahrgang',
		className: 'Klasse',
		profileIconClass: 'Profil-Icon'
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Änderungen bestätigen">
			<Text style={styles.modalText}>Bitte überprüfen Sie die folgenden Änderungen. Diese müssen von einem Administrator genehmigt werden.</Text>
			{Object.entries(changes).map(([key, values]) => (
				<View key={key} style={styles.changeRow}>
					<Text style={styles.changeLabel}>{changeLabels[key] || key}</Text>
					<View style={styles.changeValues}>
						<Text style={styles.oldValue}>{values.oldVal || 'Nicht gesetzt'}</Text>
						<Text> → </Text>
						<Text style={styles.newValue}>{values.newVal || 'Wird entfernt'}</Text>
					</View>
				</View>
			))}
			<View style={styles.modalButtons}>
				<TouchableOpacity onPress={onClose} style={[styles.button, styles.secondaryButton]} disabled={isSubmitting}>
					<Text style={styles.secondaryButtonText}>Abbrechen</Text>
				</TouchableOpacity>
				<TouchableOpacity onPress={onConfirm} style={[styles.button, styles.successButton]} disabled={isSubmitting}>
					<Text style={styles.buttonText}>{isSubmitting ? 'Wird gesendet...' : 'Bestätigen & Senden'}</Text>
				</TouchableOpacity>
			</View>
		</Modal>
	);
};

const ProfileDetails = ({ user, hasPendingRequest, onUpdate }) => {
	const [isEditing, setIsEditing] = useState(false);
	const [formData, setFormData] = useState({
		email: user.email || '',
		classYear: user.classYear ? user.classYear.toString() : '',
		className: user.className || '',
		profileIconClass: user.profileIconClass || 'fa-user-circle',
	});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
	const [detectedChanges, setDetectedChanges] = useState({});

	const handleCancel = () => {
		setFormData({
			email: user.email || '',
			classYear: user.classYear ? user.classYear.toString() : '',
			className: user.className || '',
			profileIconClass: user.profileIconClass || 'fa-user-circle',
		});
		setIsEditing(false);
		setError('');
	};

	const handleSubmit = () => {
		const changes = {};
		if (formData.email !== (user.email || '')) {
			changes.email = { oldVal: user.email, newVal: formData.email };
		}
		if (formData.classYear.toString() !== (user.classYear || '').toString()) {
			changes.classYear = { oldVal: user.classYear, newVal: formData.classYear };
		}
		if (formData.className !== (user.className || '')) {
			changes.className = { oldVal: user.className, newVal: formData.className };
		}
		if (formData.profileIconClass !== (user.profileIconClass || '')) {
			changes.profileIconClass = { oldVal: user.profileIconClass, newVal: formData.profileIconClass };
		}

		if (Object.keys(changes).length === 0) {
			addToast('Keine Änderungen vorgenommen.', 'info');
			setIsEditing(false);
			return;
		}
		setDetectedChanges(changes);
		setIsConfirmModalOpen(true);
	};
    
    const handleConfirmSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		try {
			const result = await apiClient.post('/public/profile/request-change', {
                ...formData,
                classYear: parseInt(formData.classYear, 10) || null
            });
			if (result.success) {
				addToast('Änderungsantrag erfolgreich eingereicht.', 'success');
				setIsEditing(false);
				setIsConfirmModalOpen(false);
				onUpdate();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Fehler beim Einreichen der Anfrage.');
			setIsConfirmModalOpen(false);
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<>
			<View style={styles.card}>
				<View style={styles.header}>
					<Icon name={formData.profileIconClass.replace('fa-', '')} solid size={60} color="#6c757d" />
					<Text style={styles.title}>Stammdaten</Text>
				</View>

				{hasPendingRequest && (
					<View style={styles.infoMessage}><Text>Sie haben eine ausstehende Profiländerung.</Text></View>
				)}
				{error && <Text style={styles.errorText}>{error}</Text>}
				
                <View style={styles.detailRow}>
                    <Text style={styles.label}>Benutzername:</Text>
                    <Text style={styles.value}>{user.username}</Text>
                </View>
                <View style={styles.detailRow}>
                    <Text style={styles.label}>Jahrgang:</Text>
                    <TextInput style={[styles.input, !isEditing && styles.readOnlyInput]} value={formData.classYear} onChangeText={val => setFormData({...formData, classYear: val})} editable={isEditing} keyboardType="number-pad" />
                </View>
                 <View style={styles.detailRow}>
                    <Text style={styles.label}>Klasse:</Text>
                    <TextInput style={[styles.input, !isEditing && styles.readOnlyInput]} value={formData.className} onChangeText={val => setFormData({...formData, className: val})} editable={isEditing} />
                </View>
                <View style={styles.detailRow}>
                    <Text style={styles.label}>E-Mail:</Text>
                    <TextInput style={[styles.input, !isEditing && styles.readOnlyInput]} value={formData.email} onChangeText={val => setFormData({...formData, email: val})} editable={isEditing} keyboardType="email-address" />
                </View>
                {isEditing && (
                     <View style={styles.detailRow}>
                        <Text style={styles.label}>Profil-Icon:</Text>
                        <TextInput style={styles.input} value={formData.profileIconClass} onChangeText={val => setFormData({...formData, profileIconClass: val})} placeholder="z.B. user-ninja" />
                    </View>
                )}
				
				{!hasPendingRequest && (
					<View style={styles.actionButtons}>
						{!isEditing ? (
							<TouchableOpacity onPress={() => setIsEditing(true)} style={[styles.button, styles.secondaryButton]}>
								<Text style={styles.secondaryButtonText}>Profil bearbeiten</Text>
							</TouchableOpacity>
						) : (
							<>
								<TouchableOpacity onPress={handleSubmit} style={[styles.button, styles.successButton]} disabled={isSubmitting}>
									<Text style={styles.buttonText}>Änderungen prüfen</Text>
								</TouchableOpacity>
								<TouchableOpacity onPress={handleCancel} style={[styles.button, styles.mutedButton]}>
									<Text style={styles.buttonText}>Abbrechen</Text>
								</TouchableOpacity>
							</>
						)}
					</View>
				)}
			</View>
			<ConfirmationModal isOpen={isConfirmModalOpen} onClose={() => setIsConfirmModalOpen(false)} onConfirm={handleConfirmSubmit} changes={detectedChanges} isSubmitting={isSubmitting} />
		</>
	);
};

const styles = StyleSheet.create({
    card: { backgroundColor: '#ffffff', borderRadius: 8, padding: 16, margin: 16, borderWidth: 1, borderColor: '#dee2e6' },
    header: { flexDirection: 'row', alignItems: 'center', gap: 16, marginBottom: 16 },
    title: { fontSize: 20, fontWeight: '600', color: '#002B5B' },
    infoMessage: { backgroundColor: '#0dcaf0', padding: 12, borderRadius: 6, marginBottom: 12 },
    errorText: { color: '#dc3545', marginBottom: 12 },
    detailRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingVertical: 8, borderBottomWidth: 1, borderBottomColor: '#f0f0f0' },
    label: { fontWeight: 'bold', color: '#6c757d' },
    value: { color: '#212529' },
    input: { flex: 1, textAlign: 'right', paddingVertical: 4, color: '#212529' },
    readOnlyInput: { backgroundColor: 'transparent' },
    actionButtons: { flexDirection: 'row', gap: 8, marginTop: 16 },
    button: { paddingVertical: 10, paddingHorizontal: 16, borderRadius: 6 },
    secondaryButton: { borderWidth: 1, borderColor: '#6c757d' },
    secondaryButtonText: { color: '#6c757d' },
    successButton: { backgroundColor: '#28a745' },
    mutedButton: { backgroundColor: '#6c757d' },
    buttonText: { color: '#fff', fontWeight: '500' },
    // Modal Styles
    modalText: { marginBottom: 16, fontSize: 16 },
    changeRow: { paddingVertical: 8, borderBottomWidth: 1, borderBottomColor: '#eee' },
    changeLabel: { fontWeight: 'bold', marginBottom: 4 },
    changeValues: { flexDirection: 'row', alignItems: 'center' },
    oldValue: { textDecorationLine: 'line-through', color: '#dc3545', marginRight: 8 },
    newValue: { fontWeight: 'bold', color: '#28a745', marginLeft: 8 },
    modalButtons: { flexDirection: 'row', justifyContent: 'flex-end', marginTop: 24, gap: 8 },
});

export default ProfileDetails;