import React from 'react';
import { useAuthStore } from '../../store/authStore';

const ThemeSwitcher = () => {
	const { theme, setTheme } = useAuthStore();

	const toggleTheme = () => {
		const newTheme = theme === 'light' ? 'dark' : 'light';
		setTheme(newTheme);
	};

	return (
		<button
			onClick={toggleTheme}
			className="btn btn-secondary btn-small"
			title={`Switch to ${theme === 'light' ? 'dark' : 'light'} mode`}
			style={{ padding: '0.4rem 0.6rem' }}
		>
			<i className={`fas ${theme === 'light' ? 'fa-moon' : 'fa-sun'}`}></i>
		</button>
	);
};

export default ThemeSwitcher;