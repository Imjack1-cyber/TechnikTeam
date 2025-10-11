import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, FlatList } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing, typography } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import AdminModal from './AdminModal';

const CustomPicker = ({ label, selectedValue, onValueChange, options }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const selectedLabel = options.find(opt => opt.value === selectedValue)?.label || 'Auswählen...';

    const handleSelect = (value) => {
        onValueChange(value);
        setIsModalOpen(false);
    };

    const renderItem = ({ item }) => (
        <TouchableOpacity style={styles.optionItem} onPress={() => handleSelect(item.value)}>
            <Text style={styles.optionText}>{item.label}</Text>
            {selectedValue === item.value && <Icon name="check" size={16} color={colors.primary} />}
        </TouchableOpacity>
    );

    return (
        <View style={styles.formGroup}>
            <Text style={styles.label}>{label}</Text>
            <TouchableOpacity style={styles.input} onPress={() => setIsModalOpen(true)}>
                <Text style={{color: colors.text}}>{selectedLabel}</Text>
            </TouchableOpacity>
            
            <AdminModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={label}>
                <FlatList
                    data={options}
                    renderItem={renderItem}
                    keyExtractor={item => String(item.value)}
                />
            </AdminModal>
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        optionItem: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: spacing.md,
            borderBottomWidth: 1,
            borderColor: colors.border,
        },
        optionText: {
            fontSize: typography.body,
            color: colors.text,
        },
    });
};

export default CustomPicker;