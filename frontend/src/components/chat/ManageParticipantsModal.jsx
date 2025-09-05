import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../ui/Modal';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../context/ToastContext';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import BouncyCheckbox from "react-native-bouncy-checkbox";
import Icon from '@expo/vector-icons/FontAwesome5';

const ManageParticipantsModal = ({ isOpen, onClose, onAddUsers, onRemoveUser, conversation }) => {
	const user = useAuthStore(state => state.user);
	const { data: allUsers, loading } = useApi(useCallback(() => apiClient.get('/users'), []));
	const [selectedUsers, setSelectedUsers] = useState(new Set());
	const { addToast } = useToast();

    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const existingParticipantIds = new Set(conversation.participants.map(p => p.id));
	const usersToAdd = allUsers?.filter(u => !existingParticipantIds.has(u.id));

	const handleToggleUser = (userId) => {
		setSelectedUsers(prev => {
			const newSet = new Set(prev);
			if (newSet.has(userId)) newSet.delete(userId);
			else newSet.add(userId);
			return newSet;
		});
	};

	const handleSubmit = () => {
		if (selectedUsers.size > 0) {
			onAddUsers(Array.from(selectedUsers));
		}
	};

	const handleRemoveClick = async (participant) => {
		try {
			const result = await apiClient.delete(`/public/chat/conversations/${conversation.id}/participants/${participant.id}`);
			if (result.success) {
				addToast(`${participant.username} wurde entfernt.`, 'success');
				onRemoveUser();
			} else { throw new Error(result.message); }
		} catch (err) { addToast(err.message, 'error'); }
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`"${conversation.name}" verwalten`}>
			<View>
				<Text style={styles.sectionTitle}>Aktuelle Mitglieder</Text>
				<FlatList
                    data={conversation.participants}
                    keyExtractor={item => item.id.toString()}
                    renderItem={({item: p}) => (
                        <View style={styles.participantRow}>
                            <Text>{p.username} {p.id === conversation.creatorId && '(Ersteller)'}</Text>
                            {user.id !== p.id && (
                                <TouchableOpacity onPress={() => handleRemoveClick(p)}>
                                    <Icon name="times" size={18} color={colors.danger} />
                                </TouchableOpacity>
                            )}
                        </View>
                    )}
                />
				
                <View style={styles.divider} />

				<Text style={styles.sectionTitle}>Mitglieder hinzufügen</Text>
				<View style={styles.userListContainer}>
                    {loading ? <ActivityIndicator/> : (
                        <FlatList
                            data={usersToAdd}
                            keyExtractor={item => item.id.toString()}
                            renderItem={({item}) => (
                                <BouncyCheckbox
                                    text={item.username}
                                    isChecked={selectedUsers.has(item.id)}
                                    onPress={() => handleToggleUser(item.id)}
                                    style={{paddingVertical: 8}}
                                    textStyle={{ color: colors.text, textDecorationLine: 'none' }}
                                    fillColor={colors.primary}
                                />
                            )}
                            ListEmptyComponent={<Text>Alle Benutzer sind bereits in dieser Gruppe.</Text>}
                        />
                    )}
				</View>

				<TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={handleSubmit} disabled={selectedUsers.size === 0}>
					<Text style={styles.buttonText}>Ausgewählte hinzufügen</Text>
				</TouchableOpacity>
			</View>
		</Modal>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        sectionTitle: { fontSize: typography.h4, fontWeight: 'bold', marginBottom: spacing.sm },
        participantRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: spacing.sm },
        divider: { height: 1, backgroundColor: colors.border, marginVertical: spacing.md },
        userListContainer: { maxHeight: '40%', borderWidth: 1, borderColor: colors.border, borderRadius: 8, padding: 8 },
    });
};

export default ManageParticipantsModal;