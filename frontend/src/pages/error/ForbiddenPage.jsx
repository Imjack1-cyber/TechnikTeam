import React, { useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, SafeAreaView, StatusBar } from 'react-native';
import useTypingAnimation from '../../hooks/useTypingAnimation';
import { useAuthStore } from '../../store/authStore';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { getThemeColors } from '../../styles/theme';

const ForbiddenPage = ({ navigation }) => {
	const user = useAuthStore(state => state.user);
    const theme = useAuthStore(state => state.theme);
    const isDarkMode = theme === 'dark';
    const pageStyles = styles(theme);

	const lines = useMemo(() => [
		{ text: 'Zugriffsversuch auf geschützten Bereich...', style: pageStyles.info },
		{ text: `Benutzer wird authentifiziert: ${user?.username || 'GAST'}`, style: pageStyles.info, delayAfter: 500 },
		{ text: 'Berechtigungsstufe wird geprüft...', style: pageStyles.info, delayAfter: 800 },
		{ text: '[ZUGRIFF VERWEIGERT]', style: pageStyles.fail, speed: 80 },
		{ text: 'FEHLER 403: Unzureichende Berechtigungen.', style: pageStyles.fail },
		{ text: 'Ihre aktuelle Rolle gewährt keinen Zugriff auf diese Ressource.', style: pageStyles.warn },
		{ text: 'Dieser Versuch wurde protokolliert.', style: pageStyles.info },
	], [user, pageStyles]);

	const { containerRef, renderedLines, isComplete } = useTypingAnimation(lines);

	return (
		<SafeAreaView style={pageStyles.fullScreenTerminal}>
            <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
			<View style={pageStyles.terminalHeader}>
				<View style={[pageStyles.headerDot, pageStyles.redDot]} />
				<View style={[pageStyles.headerDot, pageStyles.yellowDot]} />
				<View style={[pageStyles.headerDot, pageStyles.greenDot]} />
				<Text style={pageStyles.headerTitle}>SECURITY.LOG</Text>
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
			<View style={pageStyles.terminalFooter}>
				{isComplete && (
					<TouchableOpacity style={pageStyles.button} onPress={() => navigation.navigate('Dashboard')}>
						<Icon name="arrow-left" size={16} color="#fff" />
						<Text style={pageStyles.buttonText}>Zurück zum sicheren Bereich</Text>
					</TouchableOpacity>
				)}
			</View>
		</SafeAreaView>
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
        fullScreenTerminal: { flex: 1, backgroundColor: terminalColors.terminalBg },
        terminalHeader: { flexDirection: 'row', alignItems: 'center', padding: 16, backgroundColor: terminalColors.terminalHeaderBg, borderBottomWidth: 1, borderColor: terminalColors.terminalBorder },
        headerDot: { width: 12, height: 12, borderRadius: 6, marginRight: 8 },
        redDot: { backgroundColor: '#ff5f56' },
        yellowDot: { backgroundColor: '#ffbd2e' },
        greenDot: { backgroundColor: '#27c93f' },
        headerTitle: { color: terminalColors.terminalTextMuted, flex: 1, textAlign: 'center' },
        terminalBody: { flex: 1, padding: 16 },
        terminalLine: { flexDirection: 'row', marginBottom: 4 },
        prompt: { color: terminalColors.terminalPrompt, marginRight: 8 },
        lineText: { color: terminalColors.terminalText, flexShrink: 1, fontFamily: 'monospace' },
        cursor: { width: 8, height: 18, backgroundColor: terminalColors.terminalText, marginLeft: 2 },
        terminalFooter: { padding: 16, borderTopWidth: 1, borderTopColor: terminalColors.terminalBorder },
        button: { opacity: 1, backgroundColor: colors.primary, paddingVertical: 10, paddingHorizontal: 20, borderRadius: 6, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8 },
        buttonText: { color: colors.white, fontWeight: '500' },
        info: { color: terminalColors.terminalTextMuted },
        warn: { color: colors.warning },
        fail: { color: colors.danger },
    });
};

export default ForbiddenPage;