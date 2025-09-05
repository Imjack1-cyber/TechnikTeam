import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const EventFeedbackPage = () => {
	const route = useRoute();
	const navigation = useNavigation();
	const { eventId } = route.params;
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const [rating, setRating] = useState(0);
	const [comments, setComments] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	const apiCall = useCallback(() => apiClient.get(`/public/feedback/forms?eventId=${eventId}`), [eventId]);
	const { data, loading, error: fetchError } = useApi(apiCall);

	const handleSubmit = async () => {
		if (rating === 0) {
			setError('Bitte wählen Sie eine Sternebewertung aus.');
			return;
		}
		setIsSubmitting(true);
		setError('');

		try {
			const result = await apiClient.post('/public/feedback/event', { formId: data.form.id, rating, comments });
			if (result.success) {
				addToast('Vielen Dank für dein Feedback!', 'success');
				navigation.navigate('Profile');
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Feedback konnte nicht übermittelt werden.');
		} finally {
			setIsSubmitting(false);
		}
	};

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (fetchError) return <View style={styles.centered}><Text style={styles.errorText}>{fetchError}</Text></View>;
	if (!data) return <View style={styles.centered}><Text style={styles.errorText}>Formulardaten nicht gefunden.</Text></View>;

	if (data.alreadySubmitted) {
		return (
			<View style={styles.container}>
				<View style={styles.card}>
					<Text style={styles.title}>Feedback bereits abgegeben</Text>
					<Text style={styles.bodyText}>Vielen Dank, du hast bereits Feedback für das Event "{data.event.name}" abgegeben.</Text>
					<TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={() => navigation.navigate('Profile')}>
                        <Text style={styles.buttonText}>Zurück zum Profil</Text>
                    </TouchableOpacity>
				</View>
			</View>
		);
	}

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<View style={styles.card}>
				<Text style={styles.title}>Feedback für: {data.event.name}</Text>
				<Text style={styles.subtitle}>Dein Feedback hilft uns, zukünftige Events zu verbessern.</Text>
				{error && <Text style={styles.errorText}>{error}</Text>}
				
                <View style={styles.formGroup}>
                    <Text style={styles.label}>Gesamteindruck (1 = schlecht, 5 = super)</Text>
                    <View style={styles.starContainer}>
                        {[1, 2, 3, 4, 5].map(star => (
                            <TouchableOpacity key={star} onPress={() => setRating(star)}>
                                <Icon name="star" solid size={40} color={star <= rating ? colors.warning : colors.border} />
                            </TouchableOpacity>
                        ))}
                    </View>
                </View>
                
                <View style={styles.formGroup}>
                    <Text style={styles.label}>Kommentare & Verbesserungsvorschläge</Text>
                    <TextInput style={[styles.input, styles.textArea]} value={comments} onChangeText={setComments} multiline />
                </View>

				<TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff"/> : <Text style={styles.buttonText}>Feedback absenden</Text>}
				</TouchableOpacity>
			</View>
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    return StyleSheet.create({
        starContainer: {
            flexDirection: 'row',
            justifyContent: 'center',
            marginVertical: spacing.md,
            gap: spacing.md,
        },
    });
};

export default EventFeedbackPage;