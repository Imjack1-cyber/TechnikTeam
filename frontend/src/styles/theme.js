import { Dimensions } from 'react-native';

const { width, height } = Dimensions.get('window');

const lightColors = {
	primary: '#007bff',
	primaryHover: '#0056b3', // Note: Hover is less relevant, but good for active states
	primaryLight: 'rgba(0, 123, 255, 0.1)',
	background: '#f8f9fa',
	surface: '#ffffff',
	text: '#212529',
	heading: '#002B5B',
	textMuted: '#6c757d',
	border: '#dee2e6',
	success: '#28a745',
	danger: '#dc3545',
	warning: '#ffc107',
	info: '#0dcaf0',
	white: '#ffffff',
	black: '#000000',
};

const darkColors = {
	primary: '#58a6ff',
	primaryHover: '#80b6ff',
	primaryLight: 'rgba(88, 166, 255, 0.15)',
	background: '#0d1117',
	surface: '#161b22',
	text: '#c9d1d9',
	heading: '#58a6ff',
	textMuted: '#8b949e',
	border: '#30363d',
	success: '#56d364',
	danger: '#f87171',
	warning: '#f0b72f',
	info: '#67d4ed',
	white: '#ffffff',
	black: '#000000',
};

export const getThemeColors = (theme = 'light') => {
	return theme === 'dark' ? darkColors : lightColors;
};

export const typography = {
	h1: 28,
	h2: 24,
	h3: 20,
	h4: 18,
	body: 16,
	small: 14,
	caption: 12,
	fontWeights: {
		normal: '400',
		medium: '500',
		bold: '700',
	},
};

export const spacing = {
	xs: 4,
	sm: 8,
	md: 16,
	lg: 24,
	xl: 32,
};

export const borders = {
	radius: 8,
	width: 1,
};

export const shadows = {
	sm: {
		shadowColor: "#000",
		shadowOffset: { width: 0, height: 1 },
		shadowOpacity: 0.18,
		shadowRadius: 1.00,
		elevation: 1,
	},
	md: {
		shadowColor: "#000",
		shadowOffset: { width: 0, height: 2 },
		shadowOpacity: 0.23,
		shadowRadius: 2.62,
		elevation: 4,
	},
	lg: {
		shadowColor: "#000",
		shadowOffset: { width: 0, height: 4 },
		shadowOpacity: 0.30,
		shadowRadius: 4.65,
		elevation: 8,
	},
};

export const dimensions = {
	width,
	height,
};