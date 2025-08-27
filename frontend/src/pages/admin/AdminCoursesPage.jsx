import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, TextInput, ActivityIndicator } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';

const AdminCoursesPage = ({ navigation }) => {
	const apiCall = useCallback(() => apiClient.get('/courses'), []);
	const { data: courses, loading, error, reload } = useApi(apiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingCourse, setEditingCourse] = useState(null);
	const [formError, setFormError] = useState('');
	const { addToast } = useToast();
    
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const openModal = (course = null) => {
		setEditingCourse(course);
		setIsModalOpen(true);
	};

	const handleSubmit = async (data) => {
		try {
			const result = editingCourse
				? await apiClient.put(`/courses/${editingCourse.id}`, data)
				: await apiClient.post('/courses', data);

			if (result.success) {
				addToast(`Vorlage erfolgreich ${editingCourse ? 'aktualisiert' : 'erstellt'}.`, 'success');
				setIsModalOpen(false);
                setEditingCourse(null);
				reload();
			} else { throw new Error(result.message); }
		} catch (err) {
			setFormError(err.message || 'Ein Fehler ist aufgetreten.');
            return false; // Indicate failure to modal
		}
        return true; // Indicate success to modal
	};
    
    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>{item.name}</Text>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Abkürzung:</Text>
                <Text style={styles.value}>{item.abbreviation}</Text>
            </View>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={() => navigation.navigate('AdminMeetings', { courseId: item.id })}>
                    <Text style={styles.buttonText}>Meetings</Text>
                </TouchableOpacity>
                 <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal(item)}>
                    <Text style={styles.buttonText}>Bearbeiten</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
            <TouchableOpacity style={[styles.button, styles.successButton, styles.createButton]} onPress={() => openModal()}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neue Vorlage</Text>
            </TouchableOpacity>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			
            <FlatList
                data={courses}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
                ListHeaderComponent={() => (
                    <>
                        <Text style={styles.title}>Lehrgangs-Vorlagen</Text>
                        <Text style={styles.subtitle}>Dies sind die übergeordneten Lehrgänge. Einzelne Termine werden separat verwaltet.</Text>
                    </>
                )}
            />

			{isModalOpen && (
				<CourseModal
					isOpen={isModalOpen}
					onClose={() => setIsModalOpen(false)}
					onSubmit={handleSubmit}
					course={editingCourse}
                    formError={formError}
                    setFormError={setFormError}
				/>
			)}
		</View>
	);
};

const CourseModal = ({ isOpen, onClose, onSubmit, course, formError, setFormError }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [formData, setFormData] = useState({
        name: course?.name || '',
        abbreviation: course?.abbreviation || '',
        description: course?.description || ''
    });

    const handleChange = (name, value) => {
        setFormData(prev => ({...prev, [name]: value}));
    };

    const handleInternalSubmit = async () => {
        setIsSubmitting(true);
        const success = await onSubmit(formData);
        if (!success) {
            setIsSubmitting(false);
        }
    };
    
    return (
        <Modal isOpen={isOpen} onClose={onClose} title={course ? "Vorlage bearbeiten" : "Neue Vorlage anlegen"}>
            <View>
                {formError && <Text style={styles.errorText}>{formError}</Text>}
                <Text style={styles.label}>Name der Vorlage</Text>
                <TextInput style={styles.input} value={formData.name} onChangeText={val => handleChange('name', val)} />
                <Text style={styles.label}>Abkürzung (max. 10 Zeichen)</Text>
                <TextInput style={styles.input} value={formData.abbreviation} onChangeText={val => handleChange('abbreviation', val)} maxLength={10} />
                <Text style={styles.label}>Beschreibung</Text>
                <TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => handleChange('description', val)} multiline />

                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleInternalSubmit} disabled={isSubmitting}>
                    {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Vorlage Speichern</Text>}
                </TouchableOpacity>
            </View>
        </Modal>
    );
}

export default AdminCoursesPage;