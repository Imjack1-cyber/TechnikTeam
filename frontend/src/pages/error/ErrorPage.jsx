import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, SafeAreaView, ScrollView, Linking } from 'react-native';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing, borders } from '../../styles/theme';
import { navigationRef } from '../../router/navigation';

const ErrorPage = ({ error, location, timestamp }) => {
    const { user, theme } = useAuthStore(state => ({ user: state.user, theme: state.theme }));
    const colors = getThemeColors(theme);
    const pageStyles = styles(theme);
    const [detailsVisible, setDetailsVisible] = useState(false);

    const handleReload = () => {
        const currentRoute = navigationRef.getCurrentRoute();
        if (currentRoute) {
            // A simple way to "reload" is to replace the current screen with itself.
            navigationRef.replace(currentRoute.name, currentRoute.params);
        } else {
            // Fallback to dashboard if we can't determine the route
            navigationRef.navigate('Dashboard');
        }
    };

    const handleReport = () => {
        const subject = `TechnikTeam App - Fehlerbericht`;
        const body = `Hallo Admin-Team,

ich möchte einen Fehler melden, der in der App aufgetreten ist. Hier sind die technischen Details:

- Was: ${error?.message || 'Unbekannter Fehler'}
- Wo: ${location || 'Unbekannter Ort'}
- Wann: ${timestamp ? timestamp.toLocaleString('de-DE') : 'Unbekannter Zeitpunkt'}
- Wer: ${user?.username || 'Nicht angemeldeter Benutzer'} (ID: ${user?.id || 'N/A'})

Bitte beschreiben Sie hier, was Sie getan haben, bevor der Fehler auftrat:
[Ihre Beschreibung hier einfügen]

Vielen Dank!
`;
        const mailtoUrl = `mailto:jacques.serenz@no-bs.de?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
        Linking.openURL(mailtoUrl).catch(err => console.error("Fehler beim Öffnen des E-Mail-Clients", err));
    };

    return (
        <SafeAreaView style={pageStyles.fullScreenContainer}>
            <ScrollView contentContainerStyle={pageStyles.contentContainer}>
                <Icon name="bug" size={60} color={colors.danger} style={pageStyles.icon} />
                <Text style={pageStyles.title}>Ein unerwarteter Fehler ist aufgetreten</Text>
                <Text style={pageStyles.message}>
                    Es tut uns leid, aber etwas ist schiefgelaufen. Unser Team wurde benachrichtigt, aber Sie können uns helfen, indem Sie einen detaillierten Bericht senden.
                </Text>
                
                <View style={pageStyles.actionsContainer}>
                    <Text style={pageStyles.actionsTitle}>Was können Sie jetzt tun?</Text>
                    <View style={pageStyles.buttonRow}>
                        <TouchableOpacity style={[pageStyles.button, { backgroundColor: colors.primary }]} onPress={() => navigationRef.navigate('Dashboard')}>
                            <Icon name="home" size={16} color={colors.white} />
                            <Text style={pageStyles.buttonText}>Zum Dashboard</Text>
                        </TouchableOpacity>
                        <TouchableOpacity style={[pageStyles.button, { backgroundColor: colors.textMuted }]} onPress={handleReload}>
                            <Icon name="sync-alt" size={16} color={colors.white} />
                            <Text style={pageStyles.buttonText}>Seite neu laden</Text>
                        </TouchableOpacity>
                    </View>
                </View>

                <View style={pageStyles.detailsContainer}>
                    <TouchableOpacity style={pageStyles.detailsHeader} onPress={() => setDetailsVisible(!detailsVisible)}>
                        <Text style={pageStyles.detailsTitle}>Technische Details</Text>
                        <Icon name={detailsVisible ? 'chevron-up' : 'chevron-down'} size={16} color={colors.textMuted} />
                    </TouchableOpacity>
                    {detailsVisible && (
                        <View style={pageStyles.detailsContent}>
                            <Text style={pageStyles.detailItem}><Text style={pageStyles.detailLabel}>Was:</Text> {error?.message || 'Unbekannter Fehler'}</Text>
                            <Text style={pageStyles.detailItem}><Text style={pageStyles.detailLabel}>Wo:</Text> {location || 'Unbekannter Ort'}</Text>
                            <Text style={pageStyles.detailItem}><Text style={pageStyles.detailLabel}>Wann:</Text> {timestamp ? timestamp.toLocaleString('de-DE') : 'Unbekannter Zeitpunkt'}</Text>
                            <Text style={pageStyles.detailItem}><Text style={pageStyles.detailLabel}>Wer:</Text> {user?.username || 'Nicht angemeldet'}</Text>
                        </View>
                    )}
                </View>

                <TouchableOpacity style={[pageStyles.button, { backgroundColor: colors.success, marginTop: spacing.lg }]} onPress={handleReport}>
                    <Icon name="paper-plane" size={16} color={colors.white} />
                    <Text style={pageStyles.buttonText}>Fehler melden</Text>
                </TouchableOpacity>
            </ScrollView>
        </SafeAreaView>
    );
};

const styles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        fullScreenContainer: { flex: 1, backgroundColor: colors.background, width: '100%' },
        contentContainer: { flexGrow: 1, justifyContent: 'center', alignItems: 'center', padding: spacing.lg },
        icon: { marginBottom: spacing.lg },
        title: { fontSize: typography.h1, fontWeight: 'bold', textAlign: 'center', marginBottom: spacing.md, color: colors.heading },
        message: { fontSize: typography.body, textAlign: 'center', color: colors.textMuted, marginBottom: spacing.xl, maxWidth: 500 },
        actionsContainer: { width: '100%', maxWidth: 500, marginBottom: spacing.lg },
        actionsTitle: { fontSize: typography.h4, fontWeight: '600', color: colors.text, marginBottom: spacing.md, textAlign: 'center' },
        buttonRow: { flexDirection: 'row', justifyContent: 'center', gap: spacing.md },
        button: { paddingVertical: 12, paddingHorizontal: 24, borderRadius: borders.radius, flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
        buttonText: { color: colors.white, fontWeight: '500' },
        detailsContainer: { width: '100%', maxWidth: 500, borderWidth: 1, borderColor: colors.border, borderRadius: borders.radius, marginTop: spacing.md },
        detailsHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: spacing.md, backgroundColor: colors.background },
        detailsTitle: { fontSize: typography.body, fontWeight: 'bold', color: colors.text },
        detailsContent: { padding: spacing.md, borderTopWidth: 1, borderTopColor: colors.border },
        detailItem: { fontSize: typography.small, color: colors.text, marginBottom: spacing.sm },
        detailLabel: { fontWeight: 'bold' },
    });
};

export default ErrorPage;