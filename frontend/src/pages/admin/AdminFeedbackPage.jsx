import React, { useState, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing, borders } from '../../styles/theme';

const FeedbackColumn = ({ title, submissions, onCardClick }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	return (
		<View style={pageStyles(theme).feedbackColumn}>
			<Text style={pageStyles(theme).columnTitle}>{title}</Text>
			<ScrollView>
				{submissions.map(sub => (
					<TouchableOpacity key={sub.id} style={styles.card} onPress={() => onCardClick(sub)}>
						<Text style={pageStyles(theme).cardSubject}>{sub.subject}</Text>
						<Text style={pageStyles(theme).cardPreview} numberOfLines={2}>{sub.content}</Text>
						<Text style={pageStyles(theme).cardMeta}>Von: {sub.username}</Text>
					</TouchableOpacity>
				))}
			</ScrollView>
		</View>
	);
};

const AdminFeedbackPage = () => {
	const apiCall = useCallback(() => apiClient.get('/feedback'), []);
	const { data: submissions, loading, error, reload } = useApi(apiCall);
	const [selectedFeedback, setSelectedFeedback] = useState(null);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

	const groupedSubmissions = useMemo(() => submissions?.reduce((acc, sub) => {
		(acc[sub.status] = acc[sub.status] || []).push(sub);
		return acc;
	}, {}) || {}, [submissions]);

	const handleStatusChange = async (newStatus) => {
		if (!selectedFeedback) return;
		try {
			const result = await apiClient.put(`/feedback/${selectedFeedback.id}/status`, { status: newStatus });
			if (result.success) {
				addToast('Status erfolgreich aktualisiert', 'success');
				setSelectedFeedback(null);
				reload();
			} else { throw new Error(result.message); }
		} catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
	};

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;

	return (
		<View style={styles.container}>
			<Text style={styles.title}>Feedback-Verwaltung</Text>
			<Text style={styles.subtitle}>Verwalten Sie hier alle Benutzereinreichungen.</Text>
			<ScrollView horizontal={true} contentContainerStyle={styles.board}>
				<FeedbackColumn title="Neu" submissions={groupedSubmissions['NEW'] || []} onCardClick={setSelectedFeedback} />
				<FeedbackColumn title="Gesehen" submissions={groupedSubmissions['VIEWED'] || []} onCardClick={setSelectedFeedback} />
				<FeedbackColumn title="Geplant" submissions={groupedSubmissions['PLANNED'] || []} onCardClick={setSelectedFeedback} />
				<FeedbackColumn title="Erledigt" submissions={groupedSubmissions['COMPLETED'] || []} onCardClick={setSelectedFeedback} />
				<FeedbackColumn title="Abgelehnt" submissions={groupedSubmissions['REJECTED'] || []} onCardClick={setSelectedFeedback} />
			</ScrollView>

			{selectedFeedback && (
				<Modal isOpen={!!selectedFeedback} onClose={() => setSelectedFeedback(null)} title={selectedFeedback.subject}>
					<View style={{ flex: 1 }}>
                        <ScrollView>
                            <Text>Von: {selectedFeedback.username}</Text>
                            <Text>Eingereicht am: {new Date(selectedFeedback.submittedAt).toLocaleString('de-DE')}</Text>
                            <View style={styles.contentBox}>
                                <Text>{selectedFeedback.content}</Text>
                            </View>
                        </ScrollView>
                        <View style={styles.modalFooter}>
                            <Text style={styles.modalSectionTitle}>Status ändern:</Text>
                            <View style={styles.statusButtons}>
                                {['NEW', 'VIEWED', 'PLANNED', 'COMPLETED', 'REJECTED'].map(status => (
                                    <TouchableOpacity key={status} onPress={() => handleStatusChange(status)} style={styles.button}>
                                        <Text style={styles.buttonText}>{status}</Text>
                                    </TouchableOpacity>
                                ))}
                            </View>
                        </View>
                    </View>
				</Modal>
			)}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        board: { paddingHorizontal: spacing.md, paddingBottom: spacing.md, },
        feedbackColumn: { width: 300, backgroundColor: colors.background, borderRadius: borders.radius, padding: spacing.sm, marginRight: spacing.md },
        columnTitle: { fontSize: typography.h4, fontWeight: 'bold', borderBottomWidth: 1, borderColor: colors.border, paddingBottom: spacing.sm, marginBottom: spacing.md },
        cardSubject: { fontWeight: 'bold', marginBottom: 4 },
        cardPreview: { fontSize: typography.small, color: colors.textMuted, marginBottom: 8 },
        cardMeta: { fontSize: typography.caption, color: colors.textMuted },
        contentBox: { backgroundColor: colors.background, padding: spacing.md, borderRadius: 6, marginVertical: spacing.md },
        modalSectionTitle: { fontSize: typography.h4, fontWeight: 'bold', marginTop: spacing.lg },
        statusButtons: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginTop: spacing.sm },
    });
}

export default AdminFeedbackPage;