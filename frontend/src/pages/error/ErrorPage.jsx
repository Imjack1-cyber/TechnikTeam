import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { useRouteError } from '@react-navigation/native'; // Note: Different hook for RN
import useTypingAnimation from '../../hooks/useTypingAnimation';
import Icon from 'react-native-vector-icons/FontAwesome5';

const ErrorPage = ({ navigation }) => {
	const error = useRouteError() || { message: "Ein unbekannter Fehler ist aufgetreten." };
	console.error(error);

	const errorMessage = error?.message || "Ein unbekannter Fehler ist aufgetreten.";

	const lines = [
		{ text: 'SYSTEMDIAGNOSE WIRD GESTARTET...', style: styles.info },
		{ text: 'Speichermodule werden gescannt...', style: styles.info, delayAfter: 500 },
		{ text: '[OK] Speicherintegritätsprüfung bestanden.', style: styles.ok },
		{ text: 'Anwendungsstatus wird überprüft...', style: styles.info, delayAfter: 500 },
		{ text: `[FEHLER] Unbehandelte Ausnahme erkannt: ${errorMessage}`, style: styles.fail, delayAfter: 800 },
		{ text: 'FEHLER 500: Interner Serverfehler.', style: styles.fail },
		{ text: 'Ein kritischer Fehler ist aufgetreten.', style: styles.info },
		{ text: 'Der Systemadministrator wurde benachrichtigt.', style: styles.info },
		{ text: 'Wiederherstellungsoptionen werden vorbereitet...', style: styles.warn },
	];

	const { containerRef, renderedLines, isComplete } = useTypingAnimation(lines);

	return (
		<View style={styles.terminal}>
			<View style={styles.terminalHeader}>
				<View style={[styles.headerDot, styles.redDot]} />
				<View style={[styles.headerDot, styles.yellowDot]} />
				<View style={[styles.headerDot, styles.greenDot]} />
				<Text style={styles.headerTitle}>SYSTEM_DIAGNOSTIC.LOG</Text>
			</View>
			<ScrollView style={styles.terminalBody} ref={containerRef} contentContainerStyle={{ flexGrow: 1 }}>
				{renderedLines.map((line, index) => (
					<View key={index} style={styles.terminalLine}>
						<Text style={styles.prompt}>{'>'}</Text>
						<Text style={[styles.lineText, line.style]}>{line.text}</Text>
						{index === renderedLines.length - 1 && !isComplete && <View style={styles.cursor} />}
					</View>
				))}
			</ScrollView>
			{isComplete && (
				<TouchableOpacity style={styles.button} onPress={() => navigation.navigate('Home')}>
					<Icon name="home" size={16} color="#fff" />
					<Text style={styles.buttonText}>Zum Dashboard</Text>
				</TouchableOpacity>
			)}
		</View>
	);
};

const styles = StyleSheet.create({
    terminal: { backgroundColor: '#010409', borderWidth: 1, borderColor: '#30363d', borderRadius: 8, padding: 16, width: '100%', maxWidth: 800, maxHeight: '80%' },
    terminalHeader: { flexDirection: 'row', alignItems: 'center', paddingBottom: 16, borderBottomWidth: 1, borderColor: '#30363d', marginBottom: 16 },
    headerDot: { width: 12, height: 12, borderRadius: 6, marginRight: 8 },
    redDot: { backgroundColor: '#ff5f56' },
    yellowDot: { backgroundColor: '#ffbd2e' },
    greenDot: { backgroundColor: '#27c93f' },
    headerTitle: { color: '#8b949e', flex: 1, textAlign: 'center' },
    terminalBody: { flex: 1 },
    terminalLine: { flexDirection: 'row', marginBottom: 4 },
    prompt: { color: '#58a6ff', marginRight: 8 },
    lineText: { color: '#c9d1d9', flexShrink: 1, fontFamily: 'monospace' },
    cursor: { width: 8, height: 18, backgroundColor: '#c9d1d9', marginLeft: 2 },
    button: { opacity: 1, marginTop: 16, backgroundColor: '#007bff', paddingVertical: 10, paddingHorizontal: 20, borderRadius: 6, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8 },
    buttonText: { color: '#fff', fontWeight: '500' },
    info: { color: '#8b949e' },
    ok: { color: '#56d364' },
    warn: { color: '#f0b72f' },
    fail: { color: '#f87171' },
});


export default ErrorPage;