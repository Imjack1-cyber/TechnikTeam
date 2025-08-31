import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
// (Assuming a CourseModal component would be created similar to others)

const AdminCoursesPage = ({ navigation }) => {
	const apiCall = useCallback(() => apiClient.get('/courses'), []);
	const { data: courses, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const handleDelete = async (course) => {
		Alert.alert(`Vorlage '${course.name}' löschen?`, "Alle zugehörigen Meetings und Qualifikationen werden auch gelöscht!", [
            { text: "Abbrechen", style: "cancel" },
            { text: "Löschen", style: "destructive", onPress: async () => {
                try {
                    const result = await apiClient.delete(`/courses/${course.id}`);
                    if (result.success) {
                        addToast('Vorlage erfolgreich gelöscht.', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error'); }
            }}
        ]);
	};

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>{item.name}</Text>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Abkürzung:</Text>
                <Text>{item.abbreviation}</Text>
            </View>
            <View style={styles.cardActions}>
                <TouchableOpacity style={styles.button} onPress={() => navigation.navigate('AdminMeetings', { courseId: item.id })}>
                    <Icon name="calendar-day" size={14} />
                    <Text> Meetings</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} /* onPress={() => openModal(item)} */>
                    <Icon name="edit" size={14} color="#fff" />
                    <Text style={styles.buttonText}> Bearbeiten</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => handleDelete(item)}>
                    <Icon name="trash" size={14} color="#fff" />
                    <Text style={styles.buttonText}> Löschen</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
			<Text style={styles.title}>Lehrgangs-Vorlagen</Text>
			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			<FlatList
                data={courses}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
            />
		</View>
	);
};

export default AdminCoursesPage;