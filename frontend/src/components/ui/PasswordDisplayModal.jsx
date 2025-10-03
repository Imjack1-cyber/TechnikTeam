import React from 'react';
import { View, Text, TextInput, TouchableOpacity } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import Modal from './Modal';

const PasswordDisplayModal = ({ isOpen, onClose, username, newPassword }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    
    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Neues Passwort">
            <View>
                <Text style={styles.bodyText}>Das temporäre Passwort für <Text style={{fontWeight: 'bold'}}>{username}</Text> lautet:</Text>
                <TextInput
                    style={[styles.input, {textAlign: 'center', fontWeight: 'bold', fontSize: 18, marginVertical: 16}]}
                    value={newPassword}
                    editable={false}
                    selectTextOnFocus
                />
                <Text style={styles.bodyText}>Bitte geben Sie dieses Passwort sicher an den Benutzer weiter.</Text>
                <TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={onClose}>
                    <Text style={styles.buttonText}>Schließen</Text>
                </TouchableOpacity>
            </View>
        </Modal>
    );
};

export default PasswordDisplayModal;