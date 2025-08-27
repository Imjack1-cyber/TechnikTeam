import React, { useState, useCallback } from 'react';
import { View, Text, TextInput, StyleSheet, FlatList, TouchableOpacity, Alert, ActivityIndicator } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { Picker } from '@react-native-picker/picker';

const AdminGeoIpPage = () => {
    const apiCall = useCallback(() => apiClient.get('/admin/geoip/rules'), []);
    const { data: rules, loading, error, reload } = useApi(apiCall);
    const [newRule, setNewRule] = useState({ countryCode: '', ruleType: 'BLOCK' });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

    const handleAddRule = async () => {
        if (newRule.countryCode.length !== 2) {
            addToast('Ländercode muss genau 2 Zeichen lang sein.', 'error');
            return;
        }
        setIsSubmitting(true);
        try {
            const result = await apiClient.post('/admin/geoip/rules', newRule);
            if (result.success) {
                addToast('Regel erfolgreich gespeichert.', 'success');
                setNewRule({ countryCode: '', ruleType: 'BLOCK' });
                reload();
            } else { throw new Error(result.message); }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDeleteRule = (countryCode) => {
        Alert.alert(`Regel für "${countryCode}" löschen?`, "Diese Aktion kann nicht rückgängig gemacht werden.", [
			{ text: 'Abbrechen', style: 'cancel' },
			{ text: 'Löschen', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.delete(`/admin/geoip/rules/${countryCode}`);
                    if (result.success) {
                        addToast('Regel erfolgreich gelöscht.', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
            }},
		]);
    };
    
    const renderItem = ({ item }) => (
        <View style={styles.detailsListRow}>
            <Text style={styles.detailsListLabel}>{item.countryCode}</Text>
            <Text style={item.ruleType === 'BLOCK' ? {color: getThemeColors(theme).danger} : {color: getThemeColors(theme).success}}>{item.ruleType}</Text>
            <TouchableOpacity onPress={() => handleDeleteRule(item.countryCode)}>
                <Icon name="trash" size={18} color={getThemeColors(theme).danger} />
            </TouchableOpacity>
        </View>
    );

    return (
        <View style={styles.container}>
            <View style={styles.contentContainer}>
                <Text style={styles.title}><Icon name="globe-americas" size={24} /> GeoIP Filterregeln</Text>
                <Text style={styles.subtitle}>Verwalten Sie hier, aus welchen Ländern der Login erlaubt oder blockiert ist.</Text>
                
                <View style={styles.card}>
                    <Text style={styles.cardTitle}>Neue Regel hinzufügen</Text>
                    <View style={styles.formGroup}>
                        <Text style={styles.label}>Ländercode (ISO 3166-1 alpha-2)</Text>
                        <TextInput style={styles.input} value={newRule.countryCode} onChangeText={val => setNewRule({...newRule, countryCode: val.toUpperCase()})} maxLength={2} autoCapitalize="characters" placeholder="z.B. DE, US, CN" />
                    </View>
                    <View style={styles.formGroup}>
                        <Text style={styles.label}>Regeltyp</Text>
                        <Picker selectedValue={newRule.ruleType} onValueChange={(val) => setNewRule({...newRule, ruleType: val})}>
                            <Picker.Item label="Blockieren" value="BLOCK" />
                            <Picker.Item label="Erlauben" value="ALLOW" />
                        </Picker>
                    </View>
                    <TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleAddRule} disabled={isSubmitting}>
                        {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Hinzufügen</Text>}
                    </TouchableOpacity>
                </View>

                <View style={styles.card}>
                    <Text style={styles.cardTitle}>Bestehende Regeln</Text>
                    {loading && <ActivityIndicator />}
                    {error && <Text style={styles.errorText}>{error}</Text>}
                    <FlatList
                        data={rules}
                        renderItem={renderItem}
                        keyExtractor={item => item.countryCode}
                    />
                </View>
            </View>
        </View>
    );
};

export default AdminGeoIpPage;