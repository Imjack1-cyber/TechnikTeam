import React, { useState, useCallback } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, FlatList, ActivityIndicator } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../ui/Modal';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors } from '../../styles/theme';
import BouncyCheckbox from 'react-native-bouncy-checkbox';

const GroupChatModal = ({ isOpen, onClose, onCreateGroup }) => {
	const { data: users, loading } = useApi(useCallback(() => apiClient.get('/users'), []));
	const [groupName, setGroupName] = useState('');
	const [selectedUsers, setSelectedUsers] = useState(new Set());
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const handleToggleUser = (userId) => {
		setSelectedUsers(prev => {
			const newSet = new Set(prev);
			if (newSet.has(userId)) newSet.delete(userId);
			else newSet.add(userId);
			return newSet;
		});
	};

	const handleSubmit = () => {
		if (groupName && selectedUsers.size > 0) {
			onCreateGroup(groupName, Array.from(selectedUsers));
		}
	};

    const renderItem = ({ item }) => (
        <BouncyCheckbox
            text={item.username}
            isChecked={selectedUsers.has(item.id)}
            onPress={() => handleToggleUser(item.id)}
            style={styles.checkboxRow}
            textStyle={{ color: colors.text, textDecorationLine: 'none' }}
            fillColor={colors.primary}
        />
    );

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Neue Gruppe erstellen">
			<View>
				<Text style={styles.label}>Gruppenname</Text>
				<TextInput
					style={styles.input}
					value={groupName}
					onChangeText={setGroupName}
					placeholder="Name der neuen Gruppe"
				/>
				<Text style={styles.label}>Mitglieder auswählen</Text>
				<View style={styles.userListContainer}>
                    {loading ? <ActivityIndicator/> : (
                        <FlatList
                            data={users}
                            renderItem={renderItem}
                            keyExtractor={item => item.id.toString()}
                        />
                    )}
				</View>
				<TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={handleSubmit} disabled={!groupName || selectedUsers.size === 0}>
					<Text style={styles.buttonText}>Gruppe erstellen</Text>
				</TouchableOpacity>
			</View>
		</Modal>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        userListContainer: {
            maxHeight: '50%',
            borderWidth: 1,
            borderColor: colors.border,
            borderRadius: 8,
            padding: 8,
        },
        checkboxRow: {
            paddingVertical: 8,
        }
    });
};

export default GroupChatModal;