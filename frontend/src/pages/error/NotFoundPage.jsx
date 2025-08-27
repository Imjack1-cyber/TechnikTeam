import React, { useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { useRoute } from '@react-navigation/native';
import useTypingAnimation from '../../hooks/useTypingAnimation';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors } from '../../styles/theme';

const NotFoundPage = ({ navigation }) => {
	const route = useRoute();
	const path = route.name; // In RN, we get the route name, not the path
    const theme = useAuthStore(state => state.theme);

	const lines = useMemo(() => [
		{ text: `Führe Befehl aus: find . -name "${path}"`, style: styles(theme).info, delayAfter: 800 },
		{ text: `find: '${path}': Datei oder Verzeichnis nicht gefunden`, style: styles(theme).warn, delayAfter: 500 },
		{ text: 'FEHLER 404: Ressource nicht gefunden.', style: styles(theme).fail },
		{ text: 'Vorschlag: Die angeforderte Ressource ist nicht verfügbar.', style: styles(theme).info },
		{ text: `Führe aus: cd /home`, style: styles(theme).info },
	], [path, theme]);

	const { containerRef, renderedLines, isComplete } = useTypingAnimation(lines);

	return (
		<View style={styles(theme).terminal}>
			<View style={styles(theme).terminalHeader}>
				<View style={[styles(theme).headerDot, styles(theme).redDot]} />
				<View style={[styles(theme).headerDot, styles(theme).yellowDot]} />
				<View style={[styles(theme).headerDot, styles(theme).greenDot]} />
				<Text style={styles(theme).headerTitle}>bash</Text>
			</View>
			<ScrollView style={styles(theme).terminalBody} ref={containerRef} contentContainerStyle={{ flexGrow: 1 }}>
				{renderedLines.map((line, index) => (
					<View key={index} style={styles(theme).terminalLine}>
						<Text style={styles(theme).prompt}>{index < 1 ? '$' : '>'}</Text>
						<Text style={[styles(theme).lineText, line.style]}>{line.text}</Text>
						{index === renderedLines.length - 1 && !isComplete && <View style={styles(theme).cursor} />}
					</View>
				))}
			</ScrollView>
			{isComplete && (
				<TouchableOpacity style={styles(theme).button} onPress={() => navigation.navigate('Home')}>
					<Icon name="home" size={16} color="#fff" />
					<Text style={styles(theme).buttonText}>Zum Dashboard</Text>
				</TouchableOpacity>
			)}
		</View>
	);
};

const styles = (theme) => {
    const terminalColors = getTerminalColors(theme);
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
        button: { opacity: 1, marginTop: 16, backgroundColor: '#007bff', paddingVertical: 10, paddingHorizontal: 20, borderRadius: 6, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8 },
        buttonText: { color: '#fff', fontWeight: '500' },
        info: { color: terminalColors.terminalTextMuted },
        warn: { color: getThemeColors(theme).warning },
        fail: { color: getThemeColors(theme).danger },
    });
};

// Use a separate function to define terminal-specific colors
const getTerminalColors = (appTheme) => {
  // Always use dark theme for terminal for consistency
  return {
    terminalBg: '#010409',
    terminalHeaderBg: '#0d1117',
    terminalBorder: '#30363d',
    terminalText: '#c9d1d9',
    terminalTextMuted: '#8b949e',
    terminalPrompt: '#58a6ff',
  };
};

const getThemeColors = (theme) => theme === 'dark' ? darkColors : lightColors;

export default NotFoundPage;