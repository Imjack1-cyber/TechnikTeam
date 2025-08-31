import React from 'react';
import { TouchableOpacity, StyleSheet } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors } from '../../styles/theme';

const ThemeSwitcher = () => {
	const { theme, setTheme } = useAuthStore();
    const colors = getThemeColors(theme);

	const toggleTheme = () => {
		const newTheme = theme === 'light' ? 'dark' : 'light';
		setTheme(newTheme);
	};

	return (
		<TouchableOpacity
			onPress={toggleTheme}
			style={[styles.button, { backgroundColor: colors.surface, borderColor: colors.border }]}
		>
			<Icon name={theme === 'light' ? 'moon' : 'sun'} size={18} color={colors.text} />
		</TouchableOpacity>
	);
};

const styles = StyleSheet.create({
    button: {
        width: 40,
        height: 40,
        borderRadius: 20,
        justifyContent: 'center',
        alignItems: 'center',
        borderWidth: 1,
    }
});

export default ThemeSwitcher;