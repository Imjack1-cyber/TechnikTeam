import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator, TouchableOpacity } from 'react-native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from '@expo/vector-icons/FontAwesome5';
import Modal from '../components/ui/Modal';

const AnnouncementModal = ({ announcement, onClose }) => {
    if (!announcement) return null;

    return (
        <Modal isOpen={true} onClose={onClose} title={announcement.title}>
            <Text style={styles.subtitle}>
                Gepostet von <Text style={{ fontWeight: 'bold' }}>{announcement.authorUsername}</Text> am{" "}
                {new Date(announcement.createdAt).toLocaleDateString('de-DE')}
            </Text>
            <ScrollView style={styles.modalMarkdownContainer}>
                <MarkdownDisplay style={{ body: { padding: 12 } }}>
                    {announcement.content}
                </MarkdownDisplay>
            </ScrollView>
            <TouchableOpacity style={[styles.button, { marginTop: 16 }]} onPress={onClose}>
                <Text style={styles.buttonText}>Schließen</Text>
            </TouchableOpacity>
        </Modal>
    );
};

const AnnouncementsPage = () => {
    const apiCall = useCallback(() => apiClient.get('/public/announcements'), []);
    const { data: announcements, loading, error } = useApi(apiCall);
    const [modalData, setModalData] = useState(null);
    const [expandedIds, setExpandedIds] = useState([]); // track expanded announcements

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
        if (announcements?.length === 0) {
            return (
                <View style={styles.card}>
                    <Text>Aktuell gibt es keine neuen Mitteilungen.</Text>
                </View>
            );
        }

        return announcements?.map((post) => {
            const isLongContent = post.content.length > 500;
            const isExpanded = expandedIds.includes(post.id);

            // Show truncated preview if not expanded
            const previewContent = isLongContent && !isExpanded
                ? post.content.slice(0, 400) + " …"
                : post.content;

            return (
                <View style={styles.card} key={post.id}>
                    <Text style={styles.cardTitle}>{post.title}</Text>
                    <Text style={styles.subtitle}>
                        Gepostet von <Text style={{ fontWeight: 'bold' }}>{post.authorUsername}</Text> am{" "}
                        {new Date(post.createdAt).toLocaleDateString('de-DE')}
                    </Text>

                    {/* Render Markdown */}
                    <MarkdownDisplay>
                        {previewContent}
                    </MarkdownDisplay>

                    {isLongContent && (
                        <View style={styles.actionsRow}>
                            <TouchableOpacity
                                style={styles.readMoreButton}
                                onPress={() => toggleExpand(post.id)}
                            >
                                <Text style={styles.readMoreText}>
                                    {isExpanded ? "Weniger anzeigen" : "Mehr anzeigen"}
                                </Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                style={styles.readMoreButton}
                                onPress={() => setModalData(post)}
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
                <Icon name="thumbtack" size={24} style={styles.headerIcon} />
                <Text style={styles.title}>Anschlagbrett</Text>
            </View>
            <Text style={styles.description}>
                Wichtige und langfristige Mitteilungen für das gesamte Team.
            </Text>
            {renderContent()}
            <AnnouncementModal announcement={modalData} onClose={() => setModalData(null)} />
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

export default AnnouncementsPage;