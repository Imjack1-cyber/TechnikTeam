import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, ScrollView, TextInput } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import AdminModal from '../../components/ui/AdminModal';
import ScrollableContent from '../../components/ui/ScrollableContent';
import ConfirmationModal from '../../components/ui/ConfirmationModal';

const CourseModal = ({ isOpen, onClose, onSuccess, course }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const { addToast } = useToast();
    const [formData, setFormData] = useState({ name: '', abbreviation: '', description: '' });

    useEffect(() => {
        if (course) {
            setFormData({ name: course.name, abbreviation: course.abbreviation, description: course.description });
        } else {
            setFormData({ name: '', abbreviation: '', description: '' });
        }
    }, [course]);

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');
        try {
            const result = course 
                ? await apiClient.put(`/courses/${course.id}`, formData) 
                : await apiClient.post('/courses', formData);
            if (result.success) {
                addToast(`Vorlage erfolgreich ${course ? 'aktualisiert' : 'erstellt'}.`, 'success');
                onSuccess();
            } else { throw new Error(result.message); }
        } catch (err) {
            setError(err.message || 'Speichern fehlgeschlagen');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={course ? 'Vorlage bearbeiten' : 'Neue Vorlage'}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Speichern"
        >
            {error && <Text style={styles.errorText}>{error}</Text>}
            <Text style={styles.label}>Name</Text>
            <TextInput style={styles.input} value={formData.name} onChangeText={val => setFormData({...formData, name: val})} />
            <Text style={styles.label}>Abkürzung</Text>
            <TextInput style={styles.input} value={formData.abbreviation} onChangeText={val => setFormData({...formData, abbreviation: val})} />
            <Text style={styles.label}>Beschreibung</Text>
            <TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => setFormData({...formData, description: val})} multiline />
        </AdminModal>
    );
};


const AdminCoursesPage = ({ navigation }) => {
	const apiCall = useCallback(() => apiClient.get('/courses'), []);
	const { data: courses, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingCourse, setEditingCourse] = useState(null);
    const [deletingCourse, setDeletingCourse] = useState(null);
    const [isSubmittingDelete, setIsSubmittingDelete] = useState(false);

    const openModal = (course = null) => {
        setEditingCourse(course);
        setIsModalOpen(true);
    };

	const confirmDelete = async () => {
        if (!deletingCourse) return;
        setIsSubmittingDelete(true);
        try {
            const result = await apiClient.delete(`/courses/${deletingCourse.id}`);
            if (result.success) {
                addToast('Vorlage erfolgreich gelöscht.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error'); }
        finally {
            setIsSubmittingDelete(false);
            setDeletingCourse(null);
        }
	};
    
    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>{item.name}</Text>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Abkürzung:</Text>
                <Text style={styles.value}>{item.abbreviation}</Text>
            </View>
            <Text style={styles.description}>{item.description}</Text>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, {backgroundColor: colors.primaryLight}]} onPress={() => navigation.navigate('AdminMeetings', { courseId: item.id })}>
                    <Icon name="calendar-day" size={14} color={colors.primary} />
                    <Text style={{color: colors.primary, fontWeight: '500'}}> Meetings</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal(item)}>
                    <Icon name="edit" size={14} color={colors.white} />
                    <Text style={styles.buttonText}> Bearbeiten</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => setDeletingCourse(item)}>
                     <Icon name="trash" size={14} color={colors.danger} />
                    <Text style={styles.dangerOutlineButtonText}> Löschen</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

	return (
		<ScrollableContent style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="book" size={24} style={styles.headerIcon}/>
			    <Text style={styles.title}>Lehrgangs-Vorlagen</Text>
            </View>
             <TouchableOpacity style={[styles.button, styles.successButton, { marginHorizontal: 16, marginBottom: 16, alignSelf: 'flex-start'}]} onPress={() => openModal()}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neue Vorlage</Text>
            </TouchableOpacity>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			<FlatList
                data={courses}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{paddingHorizontal: 16}}
            />
            {isModalOpen && (
                <CourseModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={() => { setIsModalOpen(false); reload(); }} course={editingCourse} />
            )}
            {deletingCourse && (
                <ConfirmationModal
                    isOpen={!!deletingCourse}
                    onClose={() => setDeletingCourse(null)}
                    onConfirm={confirmDelete}
                    title={`Vorlage '${deletingCourse.name}' löschen?`}
                    message="Alle zugehörigen Meetings und Qualifikationen werden ebenfalls gelöscht. Diese Aktion kann nicht rückgängig gemacht werden."
                    confirmText="Löschen"
                    confirmButtonVariant="danger"
                    isSubmitting={isSubmittingDelete}
                />
            )}
		</ScrollableContent>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { padding: 16, flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        description: { color: colors.textMuted, marginVertical: 8 },
        cardActions: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, justifyContent: 'flex-end', marginTop: 16 }
    });
};

export default AdminCoursesPage;