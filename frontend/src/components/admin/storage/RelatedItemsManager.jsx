import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import useApi from '../../../hooks/useApi';
import apiClient from '../../../services/apiClient';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors } from '../../../styles/theme';
import BouncyCheckbox from "react-native-bouncy-checkbox";

const RelatedItemsManager = ({ item, allItems, onSave, onCancel }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
	const relationsApiCall = useCallback(() => apiClient.get(`/admin/storage/${item.id}/relations`), [item.id]);
	const { data: relatedItems, loading, error } = useApi(relationsApiCall);
	const [selectedIds, setSelectedIds] = useState(new Set());
	const [isSubmitting, setIsSubmitting] = useState(false);

	useEffect(() => {
		if (relatedItems) {
			setSelectedIds(new Set(relatedItems.map(i => i.id)));
		}
	}, [relatedItems]);

	const handleToggle = (itemId) => {
		setSelectedIds(prev => {
			const newSet = new Set(prev);
			if (newSet.has(itemId)) {
				newSet.delete(itemId);
			} else {
				newSet.add(itemId);
			}
			return newSet;
		});
	};

	const handleSave = async () => {
		setIsSubmitting(true);
		try {
			await apiClient.put(`/admin/storage/${item.id}/relations`, { relatedItemIds: Array.from(selectedIds) });
			onSave();
		} catch (err) {
			console.error("Failed to save related items", err);
		} finally {
			setIsSubmitting(false);
		}
	};

	const availableItems = allItems.filter(i => i.id !== item.id);

	return (
		<View>
			{loading && <ActivityIndicator />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			<View style={pageStyles.listContainer}>
				{availableItems.map(i => (
					<BouncyCheckbox
                        key={i.id}
                        text={i.name}
                        isChecked={selectedIds.has(i.id)}
                        onPress={() => handleToggle(i.id)}
                        style={pageStyles.checkboxRow}
                        textStyle={{ color: colors.text, textDecorationLine: 'none' }}
                        fillColor={colors.primary}
                    />
				))}
			</View>
			<View style={pageStyles.buttonContainer}>
				<TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={onCancel}>
                    <Text style={styles.buttonText}>Abbrechen</Text>
                </TouchableOpacity>
				<TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleSave} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff"/> : <Text style={styles.buttonText}>Beziehungen speichern</Text>}
				</TouchableOpacity>
			</View>
		</View>
	);
};

const pageStyles = StyleSheet.create({
    listContainer: {
        borderWidth: 1,
        borderColor: '#dee2e6',
        borderRadius: 8,
        padding: 8,
    },
    checkboxRow: {
        paddingVertical: 8,
    },
    buttonContainer: {
        flexDirection: 'row',
        justifyContent: 'flex-end',
        gap: 8,
        marginTop: 16,
    }
});

export default RelatedItemsManager;