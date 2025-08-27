import React, { useCallback, useState } from 'react';
import { View, Text, TextInput, FlatList, TouchableOpacity, ActivityIndicator, StyleSheet } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../ui/Modal';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors } from '../../styles/theme';

const UserSearchModal = ({ isOpen, onClose, onSelectUser }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	const { data: users, loading } = useApi(useCallback(() => apiClient.get('/users'), []));
    const [searchText, setSearchText] = useState('');

    const filteredUsers = users?.filter(u => u.username.toLowerCase().includes(searchText.toLowerCase()));

    const renderItem = ({ item }) => (
        <TouchableOpacity style={styles.listItem} onPress={() => onSelectUser(item.id)}>
            <Text style={styles.listItemText}>{item.username}</Text>
        </TouchableOpacity>
    );

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Neues Gespräch starten">
			<View>
                <TextInput 
                    style={styles.input} 
                    placeholder="Benutzer suchen..." 
                    value={searchText} 
                    onChangeText={setSearchText} 
                />
				<View style={pageStyles.listContainer}>
					{loading ? <ActivityIndicator /> : (
						<FlatList
                            data={filteredUsers}
                            renderItem={renderItem}
                            keyExtractor={item => item.id.toString()}
                            ListEmptyComponent={<Text style={{textAlign: 'center', color: getThemeColors(theme).textMuted, marginTop: 16}}>Keine Benutzer gefunden.</Text>}
                        />
					)}
				</View>
			</View>
		</Modal>
	);
};

const pageStyles = StyleSheet.create({
    listContainer: {
        maxHeight: 300, // Fixed height for scrollability
        borderWidth: 1,
        borderColor: '#dee2e6', // border-color
        borderRadius: 8,
        padding: 8,
    },
    listItem: {
        paddingVertical: 10,
        borderBottomWidth: 1,
        borderBottomColor: '#f0f0f0', // Light border for list items
    },
    listItemText: {
        fontSize: 16,
        color: '#212529', // text-color
    }
});


export default UserSearchModal;