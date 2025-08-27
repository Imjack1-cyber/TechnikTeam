import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../../components/ui/Modal';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography } from '../../styles/theme';

const AdminDocumentationPage = () => {
    const navigation = useNavigation();
	const docsApiCall = useCallback(() => apiClient.get('/admin/documentation'), []);
	const { data: docs, loading, error, reload } = useApi(docsApiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingDoc, setEditingDoc] = useState(null);
	const { addToast } = useToast();

	const openModal = (doc = null) => {
		setEditingDoc(doc);
		setIsModalOpen(true);
	};

	const handleDelete = (doc) => {
        Alert.alert(`Doku löschen?`, `"${doc.title}" wirklich löschen?`, [
            { text: 'Abbrechen', style: 'cancel' },
            { text: 'Löschen', style: 'destructive', onPress: async () => {
                try {
                    await apiClient.delete(`/admin/documentation/${doc.id}`);
                    addToast('Dokumentation gelöscht', 'success');
                    reload();
                } catch (err) { addToast(err.message, 'error'); }
            }}
        ]);
	};

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>{item.title}</Text>
            <View style={styles.detailRow}><Text style={styles.label}>Kategorie:</Text><Text style={styles.value}>{item.category}</Text></View>
            <View style={styles.detailRow}><Text style={styles.label}>Key:</Text><Text style={styles.code}>{item.pageKey}</Text></View>
            <View style={styles.detailRow}><Text style={styles.label}>Sichtbarkeit:</Text><Text style={styles.value}>{item.adminOnly ? 'Admin' : 'Alle'}</Text></View>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal(item)}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => handleDelete(item)}><Text style={styles.dangerOutlineButtonText}>Löschen</Text></TouchableOpacity>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="book" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>App-Doku verwalten</Text>
            </View>
            <TouchableOpacity style={[styles.button, styles.successButton, { margin: 16 }]} onPress={() => openModal()}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neue Seite anlegen</Text>
            </TouchableOpacity>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={docs}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{paddingHorizontal: 16}}
            />

            {/* Modal for editing would be a separate component */}
            {/* <DocumentationModal isOpen={isModalOpen} ... /> */}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingTop: 16},
        headerIcon: { color: colors.heading, marginRight: 12 },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16 },
        code: { fontFamily: 'monospace', backgroundColor: colors.background, padding: 4, borderRadius: 4 },
    });
};

export default AdminDocumentationPage;