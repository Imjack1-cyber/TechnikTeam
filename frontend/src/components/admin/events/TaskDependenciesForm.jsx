import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import BouncyCheckbox from "react-native-bouncy-checkbox";
import { getThemeColors } from '../../../styles/theme';

const TaskDependenciesForm = ({ allTasks, selectedDependencies, onDependencyChange }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);

	const handleToggle = (taskId) => {
		const newSelection = new Set(selectedDependencies);
		if (newSelection.has(taskId)) {
			newSelection.delete(taskId);
		} else {
			newSelection.add(taskId);
		}
		onDependencyChange(newSelection);
	};

	if (!allTasks || allTasks.length === 0) {
		return <Text style={{ color: colors.textMuted }}>Keine anderen Aufgaben vorhanden, von denen diese abhängen könnte.</Text>;
	}

	return (
		<View style={styles.formGroup}>
			<Text style={styles.label}>Abhängig von (Tasks, die vorher erledigt sein müssen):</Text>
			<ScrollView style={pageStyles.listContainer}>
				{allTasks.map(task => (
					<BouncyCheckbox
                        key={task.id}
                        text={task.description}
                        isChecked={selectedDependencies.has(task.id)}
                        onPress={() => handleToggle(task.id)}
                        style={pageStyles.checkboxRow}
                        textStyle={{ color: colors.text, textDecorationLine: 'none' }}
                        fillColor={colors.primary}
                    />
				))}
			</ScrollView>
		</View>
	);
};

const pageStyles = StyleSheet.create({
    listContainer: {
        maxHeight: 150,
        borderWidth: 1,
        borderColor: '#dee2e6',
        borderRadius: 8,
        padding: 8,
    },
    checkboxRow: {
        paddingVertical: 4,
    }
});

export default TaskDependenciesForm;