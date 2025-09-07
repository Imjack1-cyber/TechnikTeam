import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator, TouchableOpacity, Platform } from 'react-native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import DownloadWarningModal from '../components/ui/DownloadWarningModal';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useToast } from '../context/ToastContext';
import ProgressBar from '../components/ui/ProgressBar';

const FileLink = ({ file, navigation }) => {
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [isDownloading, setIsDownloading] = useState(false);
	const [downloadProgress, setDownloadProgress] = useState(0);
	const { addToast } = useToast();

	const isMarkdown = file.filename.toLowerCase().endsWith('.md');

	const handleDownload = async () => {
		setIsDownloading(true);
		setDownloadProgress(0);
		addToast(`Download für "${file.filename}" wird gestartet...`, 'info');

		try {
			await apiClient.downloadFile(file.id, file.filename, (progress) => {
				const currentProgress = progress.totalBytesWritten / progress.totalBytesExpectedToWrite;
				setDownloadProgress(currentProgress);
			});
			addToast(`"${file.filename}" erfolgreich heruntergeladen.`, 'success');
		} catch (error) {
			console.error('Download error:', error);
			addToast(`Download fehlgeschlagen: ${error.message}`, 'error');
		} finally {
			setIsDownloading(false);
		}
	};

	const handlePress = () => {
		if (isDownloading) return;
		if (file.needsWarning) {
			setIsModalOpen(true);
		} else {
			handleDownload();
		}
	};

	return (
		<>
			<View style={styles.fileRow}>
				<TouchableOpacity style={styles.fileLink} onPress={handlePress} disabled={isDownloading}>
					<Icon name="download" size={16} color={isDownloading ? "#ccc" : "#007bff"} />
					<View style={{ flex: 1 }}>
						<Text style={[styles.fileName, isDownloading && { color: "#ccc" }]}>{file.filename}</Text>
						{isDownloading && (
							<View style={{ marginTop: 4 }}>
								<ProgressBar progress={downloadProgress} />
							</View>
						)}
					</View>
				</TouchableOpacity>
				{isMarkdown && !isDownloading && (
					<TouchableOpacity style={styles.editButton} onPress={() => navigation.navigate('FileEditor', { fileId: file.id })}>
						<Icon name="pen-alt" size={12} color="#fff" />
						<Text style={styles.editButtonText}>Bearbeiten</Text>
					</TouchableOpacity>
				)}
			</View>
			<DownloadWarningModal
				isOpen={isModalOpen}
				onClose={() => setIsModalOpen(false)}
				onConfirm={() => {
					setIsModalOpen(false);
					handleDownload();
				}}
				file={file}
			/>
		</>
	);
};

const FilesPage = ({ navigation }) => {
	const apiCall = useCallback(() => apiClient.get('/public/files'), []);
	const { data: fileData, loading, error } = useApi(apiCall);

	const renderContent = () => {
		if (loading) return <ActivityIndicator size="large" style={{ marginTop: 20 }} />;
		if (error) return <Text style={styles.errorText}>{error}</Text>;
		if (!fileData || Object.keys(fileData).length === 0) {
			return <View style={styles.card}><Text>Es sind keine Dateien oder Dokumente verfügbar.</Text></View>;
		}

		return Object.entries(fileData).map(([categoryName, files]) => (
			<View style={styles.card} key={categoryName}>
				<View style={styles.categoryHeader}>
					<Icon name="folder" size={20} color="#002B5B" />
					<Text style={styles.categoryTitle}>{categoryName}</Text>
				</View>
				<View>
					{files.map(file => (
						<FileLink key={file.id} file={file} navigation={navigation} />
					))}
				</View>
			</View>
		));
	};

	return (
		<ScrollView style={styles.container}>
			<View style={styles.header}>
				<Icon name="folder-open" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Dateien & Dokumente</Text>
			</View>
			<Text style={styles.description}>Hier können Sie zentrale Dokumente und Vorlagen herunterladen.</Text>
			{renderContent()}
		</ScrollView>
	);
};

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: '#f8f9fa' },
    header: { flexDirection: 'row', alignItems: 'center', padding: 16 },
    headerIcon: { color: '#002B5B', marginRight: 12 },
    title: { fontSize: 24, fontWeight: '700', color: '#002B5B' },
    description: { fontSize: 16, color: '#6c757d', paddingHorizontal: 16, marginBottom: 16 },
    card: { backgroundColor: '#ffffff', borderRadius: 8, padding: 16, marginHorizontal: 16, marginBottom: 16, borderWidth: 1, borderColor: '#dee2e6' },
    categoryHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 8 },
    categoryTitle: { fontSize: 18, fontWeight: '600', color: '#002B5B', marginLeft: 8 },
    fileRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#dee2e6' },
    fileLink: { flexDirection: 'row', alignItems: 'center', gap: 8, flex: 1 },
    fileName: { color: '#007bff', fontSize: 16 },
    editButton: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#6c757d', paddingHorizontal: 10, paddingVertical: 6, borderRadius: 4, gap: 6 },
    editButtonText: { color: '#fff', fontSize: 12 },
    errorText: { color: '#dc3545', textAlign: 'center', padding: 16 },
});

export default FilesPage;