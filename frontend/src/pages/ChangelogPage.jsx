import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator, TouchableOpacity } from 'react-native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from '@expo/vector-icons/FontAwesome5';
import Modal from '../components/ui/Modal';

const ChangelogModal = ({ changelog, onClose }) => {
    if (!changelog) return null;

    return (
        <Modal isOpen={true} onClose={onClose} title={`Version ${changelog.version} - ${changelog.title}`}>
            <Text style={styles.subtitle}>
                Veröffentlicht am {new Date(changelog.releaseDate).toLocaleDateString('de-DE')}
            </Text>
            <ScrollView style={styles.modalMarkdownContainer}>
                <MarkdownDisplay style={{ body: { padding: 12 } }}>
                    {changelog.notes}
                </MarkdownDisplay>
            </ScrollView>
            <TouchableOpacity style={[styles.button, { marginTop: 16 }]} onPress={onClose}>
                <Text style={styles.buttonText}>Schließen</Text>
            </TouchableOpacity>
        </Modal>
    );
};

const ChangelogPage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/changelog'), []);
	const { data: changelogs, loading, error } = useApi(apiCall);
    const [modalData, setModalData] = useState(null);
    const [expandedIds, setExpandedIds] = useState([]);

    const toggleExpand = (id) => {
        setExpandedIds((prev) =>
            prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
        );
    };

	const renderContent = () => {
		if (loading) {
			return <ActivityIndicator size="large" color="#007bff" />;
		}
		if (error) {
			return <Text style={styles.errorText}>{error}</Text>;
		}
		if (changelogs?.length === 0) {
			return (
				<View style={styles.card}>
					<Text>Keine Changelog-Einträge vorhanden.</Text>
				</View>
			);
		}
		return changelogs?.map(cl => {
            const isLongContent = cl.notes.length > 500;
            const isExpanded = expandedIds.includes(cl.id);
            const previewContent = isLongContent && !isExpanded ? cl.notes.slice(0, 400) + " …" : cl.notes;

            return (
                <View style={styles.card} key={cl.id}>
                    <Text style={styles.cardTitle}>
                        Version {cl.version} - {cl.title}
                    </Text>
                    <Text style={styles.subtitle}>
                        Veröffentlicht am {new Date(cl.releaseDate).toLocaleDateString('de-DE')}
                    </Text>
                    <MarkdownDisplay>{previewContent}</MarkdownDisplay>
                    {isLongContent && (
                        <View style={styles.actionsRow}>
                            <TouchableOpacity
                                style={styles.readMoreButton}
                                onPress={() => toggleExpand(cl.id)}
                            >
                                <Text style={styles.readMoreText}>
                                    {isExpanded ? "Weniger anzeigen" : "Mehr anzeigen"}
                                </Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                style={styles.readMoreButton}
                                onPress={() => setModalData(cl)}
                            >
                                <Text style={styles.readMoreText}>Im Fenster öffnen</Text>
                            </TouchableOpacity>
                        </View>
                    )}
                </View>
            );
        });
	};

	return (
		<ScrollView style={styles.container}>
			<View style={styles.header}>
				<Icon name="history" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Changelogs & Neuerungen</Text>
			</View>
			<Text style={styles.description}>Hier finden Sie eine Übersicht aller wichtigen Änderungen und neuen Features der Anwendung.</Text>
			{renderContent()}
            <ChangelogModal changelog={modalData} onClose={() => setModalData(null)} />
		</ScrollView>
	);
};

const styles = StyleSheet.create({
	container: {
		flex: 1,
		backgroundColor: '#f8f9fa',
	},
	header: {
		flexDirection: 'row',
		alignItems: 'center',
		padding: 16,
	},
	headerIcon: {
		color: '#002B5B',
		marginRight: 12,
	},
	title: {
		fontSize: 24,
		fontWeight: '700',
		color: '#002B5B',
	},
	description: {
		fontSize: 16,
		color: '#6c757d',
		paddingHorizontal: 16,
		marginBottom: 16,
	},
	card: {
		backgroundColor: '#ffffff',
		borderRadius: 8,
		padding: 16,
		marginHorizontal: 16,
		marginBottom: 16,
		borderWidth: 1,
		borderColor: '#dee2e6',
	},
	cardTitle: {
		fontSize: 18,
		fontWeight: '600',
		color: '#002B5B',
	},
	subtitle: {
		color: '#6c757d',
		marginTop: 4,
		marginBottom: 12,
		fontSize: 12,
	},
	errorText: {
		color: '#dc3545',
		padding: 16,
		textAlign: 'center',
	},
    actionsRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginTop: 12,
        borderTopWidth: 1,
        borderTopColor: '#eee',
        paddingTop: 12,
    },
    readMoreButton: {
        alignItems: 'center',
    },
    readMoreText: {
        color: '#007bff',
        fontWeight: 'bold',
    },
    modalMarkdownContainer: {
        maxHeight: '80%',
        borderWidth: 1,
        borderColor: '#dee2e6',
        borderRadius: 6,
        marginTop: 12,
    },
    button: {
        backgroundColor: '#6c757d',
        padding: 12,
        borderRadius: 6,
        alignItems: 'center',
    },
    buttonText: {
        color: '#fff',
        fontWeight: '500',
    },
});

export default ChangelogPage;