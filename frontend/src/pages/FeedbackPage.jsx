import React, { useState, useCallback } from 'react';
import { View, Text, ScrollView, TextInput, TouchableOpacity, ActivityIndicator, FlatList, StyleSheet } from 'react-native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import StatusBadge from '../components/ui/StatusBadge';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const FeedbackPage = () => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

	const apiCall = useCallback(() => apiClient.get('/public/feedback/user'), []);
	const { data: submissions, loading, error, reload } = useApi(apiCall);
	const [subject, setSubject] = useState('');
	const [content, setContent] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [submitError, setSubmitError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setSubmitError('');

		try {
			const result = await apiClient.post('/public/feedback/general', { subject, content });
			if (result.success) {
				addToast('Vielen Dank! Dein Feedback wurde erfolgreich übermittelt.', 'success');
				setSubject('');
				setContent('');
				reload();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setSubmitError(err.message || 'Senden fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

    const renderSubmission = ({ item }) => (
        <View style={styles.card}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Text style={styles.cardTitle}>{item.subject}</Text>
                <StatusBadge status={item.status} />
            </View>
            <Text style={styles.subtitle}>
                Eingereicht am {new Date(item.submittedAt).toLocaleString('de-DE')}
            </Text>
            <Text style={styles.bodyText}>{item.content}</Text>
        </View>
    );

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<View style={styles.headerContainer}>
                <Icon name="lightbulb" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Feedback & Wünsche</Text>
            </View>
            <View style={styles.card}>
                <Text style={styles.cardTitle}>Neues Feedback einreichen</Text>
                <Text style={styles.subtitle}>Hast du eine Idee, einen Verbesserungsvorschlag oder ist dir ein Fehler aufgefallen?</Text>
                {submitError && <Text style={styles.errorText}>{submitError}</Text>}
                
                <Text style={styles.label}>Betreff</Text>
                <TextInput style={styles.input} value={subject} onChangeText={setSubject} placeholder="z.B. Feature-Wunsch: Dunkelmodus" />

                <Text style={styles.label}>Deine Nachricht</Text>
                <TextInput style={[styles.input, styles.textArea]} value={content} onChangeText={setContent} multiline placeholder="Bitte beschreibe deine Idee oder das Problem..." />

                <TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleSubmit} disabled={isSubmitting}>
                    {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Feedback absenden</Text>}
                </TouchableOpacity>
            </View>

            <View style={styles.card}>
                <Text style={styles.cardTitle}>Mein eingereichtes Feedback</Text>
                {loading && <ActivityIndicator />}
                {error && <Text style={styles.errorText}>{error}</Text>}
                <FlatList
                    data={submissions}
                    renderItem={renderSubmission}
                    keyExtractor={item => item.id.toString()}
                    ListEmptyComponent={<Text>Du hast noch kein Feedback eingereicht.</Text>}
                />
            </View>
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
    });
};

export default FeedbackPage;