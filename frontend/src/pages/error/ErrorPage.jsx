import React, { useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import useTypingAnimation from '../../hooks/useTypingAnimation';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors } from '../../styles/theme';

const ErrorPage = ({ navigation, error: propError }) => {
    const defaultError = { message: "Ein unbekannter Fehler ist aufgetreten." };
	const error = propError || defaultError;
	console.error(error);
    const theme = useAuthStore(state => state.theme);
    const pageStyles = styles(theme);

	const errorMessage = error?.message || "Ein unbekannter Fehler ist aufgetreten.";

	const lines = useMemo(() => [
		{ text: 'SYSTEMDIAGNOSE WIRD GESTARTET...', style: pageStyles.info },
		{ text: 'Speichermodule werden gescannt...', style: pageStyles.info, delayAfter: 500 },
		{ text: '[OK] Speicherintegritätsprüfung bestanden.', style: pageStyles.ok },
		{ text: 'Anwendungsstatus wird überprüft...', style: pageStyles.info, delayAfter: 500 },
		{ text: `[FEHLER] Unbehandelte Ausnahme erkannt: ${errorMessage}`, style: pageStyles.fail, delayAfter: 800 },
		{ text: 'FEHLER 500: Interner Serverfehler.', style: pageStyles.fail },
		{ text: 'Ein kritischer Fehler ist aufgetreten.', style: pageStyles.info },
		{ text: 'Wiederherstellungsoptionen werden vorbereitet...', style: pageStyles.warn },
	], [errorMessage, pageStyles]);

	const { containerRef, renderedLines, isComplete } = useTypingAnimation(lines);

	return (
		<View style={pageStyles.terminal}>
			<View style={pageStyles.terminalHeader}>
				<View style={[pageStyles.headerDot, pageStyles.redDot]} />
				<View style={[pageStyles.headerDot, pageStyles.yellowDot]} />
				<View style={[pageStyles.headerDot, pageStyles.greenDot]} />
				<Text style={pageStyles.headerTitle}>SYSTEM_DIAGNOSTIC.LOG</Text>
			</View>
			<ScrollView style={pageStyles.terminalBody} ref={containerRef} contentContainerStyle={{ flexGrow: 1 }}>
				{renderedLines.map((line, index) => (
					<View key={index} style={pageStyles.terminalLine}>
						<Text style={pageStyles.prompt}>{'>'}</Text>
						<Text style={[pageStyles.lineText, line.style]}>{line.text}</Text>
						{index === renderedLines.length - 1 && !isComplete && <View style={pageStyles.cursor} />}
					</View>
				))}
			</ScrollView>
			{isComplete && (
				<TouchableOpacity style={pageStyles.button} onPress={() => navigation.navigate('Dashboard')}>
					<Icon name="home" size={16} color="#fff" />
					<Text style={pageStyles.buttonText}>Zum Dashboard</Text>
				</TouchableOpacity>
			)}
		</View>
	);
};

const getTerminalColors = () => ({
    terminalBg: '#010409',
    terminalHeaderBg: '#0d1117',
    terminalBorder: '#30363d',
    terminalText: '#c9d1d9',
    terminalTextMuted: '#8b949e',
    terminalPrompt: '#58a6ff',
});

const styles = (theme) => {
    const terminalColors = getTerminalColors();
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        terminal: { backgroundColor: terminalColors.terminalBg, borderWidth: 1, borderColor: terminalColors.terminalBorder, borderRadius: 8, padding: 16, width: '100%', maxWidth: 800, maxHeight: '80%' },
        terminalHeader: { flexDirection: 'row', alignItems: 'center', paddingBottom: 16, borderBottomWidth: 1, borderColor: terminalColors.terminalBorder, marginBottom: 16 },
        headerDot: { width: 12, height: 12, borderRadius: 6, marginRight: 8 },
        redDot: { backgroundColor: '#ff5f56' },
        yellowDot: { backgroundColor: '#ffbd2e' },
        greenDot: { backgroundColor: '#27c93f' },
        headerTitle: { color: terminalColors.terminalTextMuted, flex: 1, textAlign: 'center' },
        terminalBody: { flex: 1 },
        terminalLine: { flexDirection: 'row', marginBottom: 4 },
        prompt: { color: terminalColors.terminalPrompt, marginRight: 8 },
        lineText: { color: terminalColors.terminalText, flexShrink: 1, fontFamily: 'monospace' },
        cursor: { width: 8, height: 18, backgroundColor: terminalColors.terminalText, marginLeft: 2 },
        button: { opacity: 1, marginTop: 16, backgroundColor: colors.primary, paddingVertical: 10, paddingHorizontal: 20, borderRadius: 6, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8 },
        buttonText: { color: colors.white, fontWeight: '500' },
        info: { color: terminalColors.terminalTextMuted },
        ok: { color: colors.success },
        warn: { color: colors.warning },
        fail: { color: colors.danger },
    });
};

export default ErrorPage;