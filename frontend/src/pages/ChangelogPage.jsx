import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator, TouchableOpacity } from 'react-native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from 'react-native-vector-icons/FontAwesome5';
import Modal from '../components/ui/Modal';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing, typography } from '../styles/theme';

const ChangelogModal = ({ changelog, onClose }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const pageSpecificStyles = pageStyles(theme);

    if (!changelog) return null;

    return (
        <Modal isOpen={true} onClose={onClose} title={`Version ${changelog.version} - ${changelog.title}`}>
            <Text style={styles.subtitle}>
                Veröffentlicht am {new Date(changelog.releaseDate).toLocaleDateString('de-DE')}
            </Text>
            <ScrollView style={pageSpecificStyles.modalMarkdownContainer}>
                <MarkdownDisplay style={{ body: { padding: 12, color: getThemeColors(theme).text } }}>
                    {changelog.notes}
                </MarkdownDisplay>
            </ScrollView>
            <TouchableOpacity style={[styles.button, styles.secondaryButton, { marginTop: 16 }]} onPress={onClose}>
                <Text style={styles.buttonText}>Schließen</Text>
            </TouchableOpacity>
        </Modal>
    );
};

const ChangelogPage = () => {
    const apiCall = useCallback(() => apiClient.get('/public/changelog'), []);
    const { data: changelogs, loading, error } = useApi(apiCall, { subscribeTo: 'CHANGELOG' });
    const [modalData, setModalData] = useState(null);
    const [expandedIds, setExpandedIds] = useState([]);
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
        if (changelogs?.length === 0) {
            return (
                <View style={styles.card}>
                    <Text style={styles.bodyText}>Keine Changelog-Einträge vorhanden.</Text>
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
                    <MarkdownDisplay style={{ body: { color: colors.text } }}>{previewContent}</MarkdownDisplay>
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

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: {
            flex: 1,
        },
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
        title: {
            fontSize: typography.h2,
            fontWeight: '700',
        },
        description: {
            paddingHorizontal: 16,
            marginVertical: 8,
        },
        actionsRow: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            marginTop: 12,
            paddingTop: 12,
            borderTopWidth: 1,
            borderTopColor: colors.border,
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

export default ChangelogPage;