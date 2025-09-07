import React, { useState, useEffect, useCallback, useRef } from 'react';
import { View, Text, StyleSheet, FlatList, TextInput, TouchableOpacity, ActivityIndicator, Alert, Linking } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import useWebSocket from '../../hooks/useWebSocket';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import ManageParticipantsModal from './ManageParticipantsModal';
import MessageStatus from './MessageStatus';
import { useToast } from '../../context/ToastContext';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing, typography, borders } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import MarkdownDisplay from 'react-native-markdown-display';

const MessageView = () => {
	const route = useRoute();
	const navigation = useNavigation();
	const { conversationId } = route.params;
	const user = useAuthStore(state => state.user);
	const fileInputRef = useRef(null);
	const [newMessage, setNewMessage] = useState('');
	const [messages, setMessages] = useState([]);
	const [conversation, setConversation] = useState(null);
	const [isManageModalOpen, setIsManageModalOpen] = useState(false);
	const [editingMessageId, setEditingMessageId] = useState(null);
	const [editingText, setEditingText] = useState('');
	const { addToast } = useToast();
	const theme = useAuthStore(state => state.theme);
	const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
	const colors = getThemeColors(theme);

	const messagesApiCall = useCallback(() => apiClient.get(`/public/chat/conversations/${conversationId}/messages`), [conversationId]);
	const { data: initialMessages, loading: messagesLoading, error: messagesError, reload: reloadMessages } = useApi(messagesApiCall);

	const conversationApiCall = useCallback(() => apiClient.get(`/public/chat/conversations/${conversationId}`), [conversationId]);
	const { data: currentConversation, reload: reloadConversation } = useApi(conversationApiCall);

	useEffect(() => {
		if (currentConversation) setConversation(currentConversation);
	}, [currentConversation]);

	const handleWebSocketMessage = useCallback((message) => {
		if (message.type === 'new_message') setMessages(prev => [message.payload, ...prev]);
		else if (message.type === 'messages_status_updated') setMessages(prev => prev.map(msg => message.payload.messageIds.includes(msg.id) ? { ...msg, status: message.payload.newStatus } : msg));
		else if (message.type === 'message_updated' || message.type === 'message_deleted') setMessages(prev => prev.map(msg => msg.id === message.payload.id ? message.payload : msg));
	}, []);

	const { sendMessage } = useWebSocket(`/ws/dm/${conversationId}`, handleWebSocketMessage);

	useEffect(() => { if (initialMessages) setMessages(initialMessages); }, [initialMessages]);

	useEffect(() => {
		const unreadMessageIds = messages.filter(msg => msg.senderId !== user.id && msg.status !== 'READ').map(msg => msg.id);
		if (unreadMessageIds.length > 0) sendMessage({ type: 'mark_as_read', payload: { messageIds: unreadMessageIds } });
	}, [messages, user.id, sendMessage]);

	const handleSubmit = () => {
		if (newMessage.trim()) {
			sendMessage({ type: 'new_message', payload: { messageText: newMessage } });
			setNewMessage('');
		}
	};

	const renderMessageContent = (msg) => {
		const isSentByMe = msg.senderId === user.id;
		const fileRegex = /\[(.*?)\]\((.*?)\)/;
		const fileMatch = msg.messageText.match(fileRegex);
		if (fileMatch) {
			const fileName = fileMatch[1];
			const fileUrl = fileMatch[2];
			return <TouchableOpacity onPress={() => Linking.openURL(fileUrl)}><Text style={{ color: isSentByMe ? colors.white : colors.text }}><Icon name="file-alt" /> {fileName}</Text></TouchableOpacity>;
		}
		return <MarkdownDisplay style={{ body: { color: isSentByMe ? colors.white : colors.text } }}>{msg.messageText}</MarkdownDisplay>;
	};

	const getHeaderText = () => {
		if (!conversation) return 'Lade...';
		if (conversation.groupChat) return conversation.name;
		const other = conversation.participants?.find(p => p.id !== user.id);
		return other ? other.username : 'Unbekannt';
	};

	return (
		<View style={styles.container}>
			<View style={styles.header}>
				<TouchableOpacity onPress={() => navigation.goBack()}><Icon name="arrow-left" size={20} /></TouchableOpacity>
				<Text style={styles.headerTitle}>{getHeaderText()}</Text>
				{conversation?.groupChat && conversation.creatorId === user.id && <TouchableOpacity onPress={() => setIsManageModalOpen(true)}><Icon name="user-plus" size={20} /></TouchableOpacity>}
			</View>
			<FlatList
				data={messages}
				inverted
				keyExtractor={item => item.id.toString()}
				renderItem={({ item: msg }) => {
					const isSentByMe = msg.senderId === user.id;
					return (
						<View style={[styles.bubbleContainer, isSentByMe ? styles.sent : styles.received]}>
							<View style={[styles.bubble, isSentByMe ? { backgroundColor: colors.primary } : { backgroundColor: msg.chatColor || colors.background }]}>
								{!isSentByMe && <Text style={[styles.sender, { color: colors.primary }]}>{msg.senderUsername}</Text>}
								{renderMessageContent(msg)}
								<View style={styles.metaContainer}>
									<Text style={[styles.timestamp, isSentByMe && { color: 'rgba(255,255,255,0.7)' }]}>{new Date(msg.sentAt).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })}</Text>
									<MessageStatus status={msg.status} isSentByMe={isSentByMe} />
								</View>
							</View>
						</View>
					)
				}}
				contentContainerStyle={{ padding: spacing.md }}
			/>
			<View style={styles.inputContainer}>
				<TextInput style={styles.input} value={newMessage} onChangeText={setNewMessage} placeholder="Nachricht schreiben..." />
				<TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit}><Text style={styles.buttonText}>Senden</Text></TouchableOpacity>
			</View>
			{isManageModalOpen && conversation && <ManageParticipantsModal isOpen={isManageModalOpen} onClose={() => setIsManageModalOpen(false)} onAddUsers={() => { }} onRemoveUser={() => { }} conversation={conversation} />}
		</View>
	);
};

const pageStyles = (theme) => {
	const colors = getThemeColors(theme);
	return StyleSheet.create({
		header: { flexDirection: 'row', alignItems: 'center', padding: spacing.md, borderBottomWidth: 1, borderColor: colors.border, backgroundColor: colors.surface },
		headerTitle: { flex: 1, fontSize: typography.h4, fontWeight: 'bold', textAlign: 'center' },
		bubbleContainer: { flexDirection: 'row', maxWidth: '80%', marginVertical: spacing.xs },
		sent: { alignSelf: 'flex-end', justifyContent: 'flex-end' },
		received: { alignSelf: 'flex-start', justifyContent: 'flex-start' },
		bubble: { padding: spacing.sm, borderRadius: 18 },
		sender: { fontWeight: 'bold', fontSize: typography.small, marginBottom: 2 },
		metaContainer: { flexDirection: 'row', alignSelf: 'flex-end', alignItems: 'center', gap: spacing.xs, marginTop: 4 },
		timestamp: { fontSize: typography.caption, color: colors.textMuted },
		inputContainer: { flexDirection: 'row', padding: spacing.sm, borderTopWidth: 1, borderColor: colors.border, backgroundColor: colors.surface, gap: spacing.sm },
		input: { flex: 1, borderWidth: 1, borderColor: colors.border, borderRadius: 20, paddingHorizontal: spacing.md, backgroundColor: colors.background }
	});
};

export default MessageView;