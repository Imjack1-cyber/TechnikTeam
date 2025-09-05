import React from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';
import { Picker } from '@react-native-picker/picker';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors, spacing } from '../../../styles/theme';

const DynamicSkillRows = ({ rows, setRows, courses }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const handleRowChange = (index, field, value) => {
		const newRows = [...rows];
		newRows[index][field] = value;
		setRows(newRows);
	};

	const handleAddRow = () => {
		setRows([...rows, { requiredCourseId: '', requiredPersons: 1 }]);
	};

	const handleRemoveRow = (index) => {
		setRows(rows.filter((_, i) => i !== index));
	};

	return (
		<View>
			{rows.map((row, index) => (
				<View style={styles.rowContainer} key={index}>
					<View style={styles.pickerContainer}>
                        <Picker
                            selectedValue={row.requiredCourseId}
                            onValueChange={(val) => handleRowChange(index, 'requiredCourseId', val)}
                        >
                            <Picker.Item label="-- Qualifikation auswählen --" value="" />
                            {courses.map(course => <Picker.Item key={course.id} label={course.name} value={course.id} />)}
                        </Picker>
                    </View>
                    <TextInput
                        style={styles.quantityInput}
                        value={String(row.requiredPersons)}
                        onChangeText={(val) => handleRowChange(index, 'requiredPersons', val)}
                        keyboardType="number-pad"
                    />
                    <TouchableOpacity onPress={() => handleRemoveRow(index)}>
                        <Icon name="times-circle" size={24} color={colors.danger} />
                    </TouchableOpacity>
				</View>
			))}
			<TouchableOpacity style={[styles.button, styles.secondaryButton, {alignSelf: 'flex-start'}]} onPress={handleAddRow}>
                <Icon name="plus" size={14} color={colors.text} />
				<Text style={{color: colors.text}}>Anforderung hinzufügen</Text>
			</TouchableOpacity>
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        rowContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            gap: spacing.sm,
            marginBottom: spacing.sm,
        },
        pickerContainer: {
            flex: 1,
            borderWidth: 1,
            borderColor: colors.border,
            borderRadius: 8,
        },
        quantityInput: {
            width: 70,
            height: 48,
            borderWidth: 1,
            borderColor: colors.border,
            borderRadius: 8,
            paddingHorizontal: spacing.sm,
            textAlign: 'center',
        },
    });
};

export default DynamicSkillRows;