import React, { useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, SafeAreaView, StatusBar } from 'react-native';
import useTypingAnimation from '../../hooks/useTypingAnimation';
import { useAuthStore } from '../../store/authStore';
import Icon from 'react-native-vector-icons/FontAwesome5';

const ForbiddenPage = ({ navigation }) => {
	const user = useAuthStore(state => state.user);
    const theme = useAuthStore(state => state.theme);
    const isDarkMode = theme === 'dark';

	const lines = useMemo(() => [
		{ text: 'Zugriffsversuch auf geschützten Bereich...', style: styles(theme).info },
		{ text: `Benutzer wird authentifiziert: ${user?.username || 'GAST'}`, style: styles(theme).info, delayAfter: 500 },
		{ text: 'Berechtigungsstufe wird geprüft...', style: styles(theme).info, delayAfter: 800 },
		{ text: '[ZUGRIFF VERWEIGERT]', style: styles(theme).fail, speed: 80 },
		{ text: 'FEHLER 403: Unzureichende Berechtigungen.', style: styles(theme).fail },
		{ text: 'Ihre aktuelle Rolle gewährt keinen Zugriff auf diese Ressource.', style: styles(theme).warn },
		{ text: 'Dieser Versuch wurde protokolliert.', style: styles(theme).info },
	], [user, theme]);

	const { containerRef, renderedLines, isComplete } = useTypingAnimation(lines);

	return (
		<SafeAreaView style={styles(theme).fullScreenTerminal}>
            <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
			<View style={styles(theme).terminalHeader}>
				<View style={[styles(theme).headerDot, styles(theme).redDot]} />
				<View style={[styles(theme).headerDot, styles(theme).yellowDot]} />
				<View style={[styles(theme).headerDot, styles(theme).greenDot]} />
				<Text style={styles(theme).headerTitle}>SECURITY.LOG</Text>
			</View>
			<ScrollView style={styles(theme).terminalBody} ref={containerRef} contentContainerStyle={{ flexGrow: 1 }}>
				{renderedLines.map((line, index) => (
					<View key={index} style={styles(theme).terminalLine}>
						<Text style={styles(theme).prompt}>{'>'}</Text>
						<Text style={[styles(theme).lineText, line.style]}>{line.text}</Text>
						{index === renderedLines.length - 1 && !isComplete && <View style={styles(theme).cursor} />}
					</View>
				))}
			</ScrollView>
			<View style={styles(theme).terminalFooter}>
				{isComplete && (
					<TouchableOpacity style={styles(theme).button} onPress={() => navigation.navigate('Home')}>
						<Icon name="arrow-left" size={16} color="#fff" />
						<Text style={styles(theme).buttonText}>Zurück zum sicheren Bereich</Text>
					</TouchableOpacity>
				)}
			</View>
		</SafeAreaView>
	);
};

const styles = (theme) => {
    const colors = getThemeColors(theme === 'dark' ? 'dark' : 'light'); // Use light terminal theme for light mode app
    return StyleSheet.create({
        fullScreenTerminal: { flex: 1, backgroundColor: colors.terminalBg, color: colors.terminalText },
        terminalHeader: { flexDirection: 'row', alignItems: 'center', padding: 16, backgroundColor: colors.terminalHeaderBg, borderBottomWidth: 1, borderColor: colors.terminalBorder },
        headerDot: { width: 12, height: 12, borderRadius: 6, marginRight: 8 },
        redDot: { backgroundColor: '#ff5f56' },
        yellowDot: { backgroundColor: '#ffbd2e' },
        greenDot: { backgroundColor: '#27c93f' },
        headerTitle: { color: colors.terminalTextMuted, flex: 1, textAlign: 'center' },
        terminalBody: { flex: 1, padding: 16 },
        terminalLine: { flexDirection: 'row', marginBottom: 4 },
        prompt: { color: colors.terminalPrompt, marginRight: 8 },
        lineText: { color: colors.terminalText, flexShrink: 1, fontFamily: 'monospace' },
        cursor: { width: 8, height: 18, backgroundColor: colors.terminalText, marginLeft: 2 },
        terminalFooter: { padding: 16, borderTopWidth: 1, borderTopColor: colors.terminalBorder },
        button: { opacity: 1, backgroundColor: '#007bff', paddingVertical: 10, paddingHorizontal: 20, borderRadius: 6, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8 },
        buttonText: { color: '#fff', fontWeight: '500' },
        info: { color: colors.terminalTextMuted },
        warn: { color: getThemeColors(theme).warning },
        fail: { color: getThemeColors(theme).danger },
    });
};

// Define terminal colors separately
const getTerminalColors = (appTheme) => {
    return {
        terminalBg: '#010409',
        terminalHeaderBg: '#0d1117',
        terminalBorder: '#30363d',
        terminalText: '#c9d1d9',
        terminalTextMuted: '#8b949e',
        terminalPrompt: '#58a6ff',
    };
};
// Helper to avoid re-defining styles
const getThemeColors = (theme) => theme === 'dark' ? darkColors : lightColors;

export default ForbiddenPage;