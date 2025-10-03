import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import { useNavigation, useRoute } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import GroupChatModal from './GroupChatModal';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing, borders } from '../../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';
import UserSearchModal from './UserSearchModal';
import ConfirmationModal from '../ui/ConfirmationModal';

const ConversationList = () => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const navigation = useNavigation();
    const route = useRoute(); // Access route to get current conversationId if any
    const selectedConversationId = parseInt(route.params?.conversationId, 10);

	const apiCall = useCallback(() => apiClient.get('/public/chat/conversations'), []);
	const { data: conversations, loading, error, reload } = useApi(apiCall);
	const [isUserSearchModalOpen, setIsUserSearchModalOpen] = useState(false);
	const [isGroupChatModalOpen, setIsGroupChatModalOpen] = useState(false);
    const [deletingConversation, setDeletingConversation] = useState(null);
    const [isSubmittingDelete, setIsSubmittingDelete] = useState(false);
	const { addToast } = useToast();

    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const handleSelectUser = async (userId) => {
		setIsUserSearchModalOpen(false);
		try {
			const result = await apiClient.post('/public/chat/conversations', { userId });
			if (result.success && result.data.conversationId) {
				navigation.navigate('MessageView', { conversationId: result.data.conversationId });
			} else { throw new Error(result.message || 'Gespräch konnte nicht gestartet werden.'); }
		} catch (err) { addToast(err.message, 'error'); }
	};

	const handleCreateGroup = async (name, participantIds) => {
		setIsGroupChatModalOpen(false);
		try {
			const result = await apiClient.post('/public/chat/conversations/group', { name, participantIds });
			if (result.success && result.data.conversationId) {
				addToast('Gruppe erfolgreich erstellt!', 'success');
				reload();
				navigation.navigate('MessageView', { conversationId: result.data.conversationId });
			} else { throw new Error(result.message || 'Gruppe konnte nicht erstellt werden.'); }
		} catch (err) { addToast(err.message, 'error'); }
	};

	const handleDeleteGroup = (conv) => {
		setDeletingConversation(conv);
	};

    const confirmDeleteGroup = async () => {
        if (!deletingConversation) return;
        setIsSubmittingDelete(true);

        try {
            const result = await apiClient.delete(`/public/chat/conversations/${deletingConversation.id}`);
            if (result.success) {
                addToast('Gruppe wurde gelöscht.', 'success');
                if (selectedConversationId === deletingConversation.id) {
                    navigation.navigate('ConversationList');
                }
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(err.message, 'error'); }
        finally {
            setDeletingConversation(null);
            setIsSubmittingDelete(false);
        }
    };
    
    const renderConversationItem = ({ item: conv }) => (
        <TouchableOpacity 
            style={[styles.conversationItem, conv.id === selectedConversationId && styles.activeConversationItem]} 
            onPress={() => navigation.navigate('MessageView', { conversationId: conv.id })}
        >
            <Icon name={conv.groupChat ? 'users' : 'user'} size={24} color={styles.conversationIcon.color} />
            <View style={styles.conversationDetails}>
                <Text style={styles.conversationUsername}>{conv.groupChat ? conv.name : conv.otherParticipantUsername}</Text>
                <Text style={styles.conversationSnippet} numberOfLines={1}>{conv.lastMessage}</Text>
            </View>
            {conv.groupChat && (conv.creatorId === user.id || isAdmin) && (
                <TouchableOpacity onPress={() => handleDeleteGroup(conv)}>
                    <Icon name="trash" size={18} color={colors.danger} />
                </TouchableOpacity>
            )}
        </TouchableOpacity>
    );

	return (
		<View style={styles.container}>
			<View style={styles.header}>
				<Text style={styles.headerTitle}>Gespräche</Text>
				<View style={styles.actions}>
					<TouchableOpacity style={styles.button} onPress={() => setIsUserSearchModalOpen(true)}>
						<Icon name="user-plus" size={16} color={colors.white} />
					</TouchableOpacity>
					<TouchableOpacity style={styles.button} onPress={() => setIsGroupChatModalOpen(true)}>
						<Icon name="users" size={16} color={colors.white} />
					</TouchableOpacity>
				</View>
			</View>
			<View style={styles.listContainer}>
				{loading && <ActivityIndicator size="large" />}
				{error && <Text style={styles.errorText}>{error}</Text>}
				<FlatList
                    data={conversations}
                    renderItem={renderConversationItem}
                    keyExtractor={item => item.id.toString()}
                    ListEmptyComponent={<Text style={styles.emptyListText}>Keine Gespräche vorhanden.</Text>}
                />
			</View>
			<UserSearchModal isOpen={isUserSearchModalOpen} onClose={() => setIsUserSearchModalOpen(false)} onSelectUser={handleSelectUser} />
			<GroupChatModal isOpen={isGroupChatModalOpen} onClose={() => setIsGroupChatModalOpen(false)} onCreateGroup={handleCreateGroup} />
            {deletingConversation && (
                <ConfirmationModal
                    isOpen={!!deletingConversation}
                    onClose={() => setDeletingConversation(null)}
                    onConfirm={confirmDeleteGroup}
                    title={`Gruppe "${deletingConversation.name}" löschen?`}
                    message="Dies kann nicht rückgängig gemacht werden und löscht die Gruppe für alle Mitglieder."
                    confirmText="Löschen"
                    confirmButtonVariant="danger"
                    isSubmitting={isSubmittingDelete}
                />
            )}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1, backgroundColor: colors.surface },
        header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: spacing.md, borderBottomWidth: 1, borderColor: colors.border },
        headerTitle: { fontSize: typography.h3, fontWeight: 'bold' },
        actions: { flexDirection: 'row', gap: spacing.sm },
        button: { padding: spacing.sm, backgroundColor: colors.primary, borderRadius: borders.radius },
        listContainer: { flex: 1 },
        conversationItem: { flexDirection: 'row', alignItems: 'center', padding: spacing.md, borderBottomWidth: 1, borderColor: colors.border },
        activeConversationItem: { backgroundColor: colors.primaryLight, borderRightWidth: 3, borderRightColor: colors.primary },
        conversationIcon: { color: colors.textMuted, marginRight: spacing.md },
        conversationDetails: { flex: 1 },
        conversationUsername: { fontWeight: 'bold', fontSize: typography.body },
        conversationSnippet: { fontSize: typography.small, color: colors.textMuted },
        emptyListText: { padding: spacing.md, textAlign: 'center', color: colors.textMuted },
    });
};

export default ConversationList;