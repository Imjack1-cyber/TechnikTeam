import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator, TouchableOpacity } from 'react-native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from '@expo/vector-icons/FontAwesome5';
import Modal from '../components/ui/Modal';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../styles/theme';

const AnnouncementModal = ({ announcement, onClose }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const pageSpecificStyles = pageStyles(theme);
    const colors = getThemeColors(theme);

    if (!announcement) return null;

    return (
        <Modal isOpen={true} onClose={onClose} title={announcement.title}>
            <Text style={styles.subtitle}>
                Gepostet von <Text style={{ fontWeight: 'bold' }}>{announcement.authorUsername}</Text> am{" "}
                {new Date(announcement.createdAt).toLocaleDateString('de-DE')}
            </Text>
            <ScrollView style={pageSpecificStyles.modalMarkdownContainer}>
                <MarkdownDisplay style={{ body: { padding: 12, color: colors.text } }}>
                    {announcement.content}
                </MarkdownDisplay>
            </ScrollView>
            <TouchableOpacity style={[styles.button, styles.secondaryButton, { marginTop: 16 }]} onPress={onClose}>
                <Text style={styles.buttonText}>Schließen</Text>
            </TouchableOpacity>
        </Modal>
    );
};

const AnnouncementsPage = () => {
    const apiCall = useCallback(() => apiClient.get('/public/announcements'), []);
    const { data: announcements, loading, error } = useApi(apiCall, { subscribeTo: 'ANNOUNCEMENT' });
    const [modalData, setModalData] = useState(null);
    const [expandedIds, setExpandedIds] = useState([]); // track expanded announcements
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const toggleExpand = (id) => {
        setExpandedIds((prev) =>
            prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
        );
    };

    const renderContent = () => {
        if (loading) {
            return <ActivityIndicator size="large" color={colors.primary} />;
        }
        if (error) {
            return <Text style={styles.errorText}>{error}</Text>;
        }
        if (announcements?.length === 0) {
            return (
                <View style={styles.card}>
                    <Text style={styles.bodyText}>Aktuell gibt es keine neuen Mitteilungen.</Text>
                </View>
            );
        }

        return announcements?.map((post) => {
            const isLongContent = post.content.length > 500;
            const isExpanded = expandedIds.includes(post.id);

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

                    <MarkdownDisplay style={{ body: { color: colors.text } }}>
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

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: {
            flexDirection: 'row',
            alignItems: 'center',
            paddingHorizontal: 16,
            paddingTop: 16,
        },
        headerIcon: {
            color: colors.heading,
            marginRight: 12,
        },
        description: {
            fontSize: 16,
            color: colors.textMuted,
            paddingHorizontal: 16,
            marginBottom: 16,
        },
        actionsRow: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            marginTop: 12,
            borderTopWidth: 1,
            borderTopColor: colors.border,
            paddingTop: 12,
        },
        readMoreButton: {
            alignItems: 'center',
        },
        readMoreText: {
            color: colors.primary,
            fontWeight: 'bold',
        },
        modalMarkdownContainer: {
            maxHeight: '80%',
            borderWidth: 1,
            borderColor: colors.border,
            borderRadius: 6,
            marginTop: 12,
        },
    });
};

export default AnnouncementsPage;