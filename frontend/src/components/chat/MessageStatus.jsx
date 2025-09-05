import React from 'react';
import { View } from 'react-native';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors } from '../../styles/theme';

const MessageStatus = ({ status, isSentByMe }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);

	if (!isSentByMe) {
		return null;
	}

	const getIcon = () => {
		switch (status) {
			case 'SENT':
				return <Icon name="check" size={12} color="rgba(255, 255, 255, 0.7)" />;
			case 'DELIVERED':
				return <Icon name="check-double" size={12} color="rgba(255, 255, 255, 0.7)" />;
			case 'READ':
				return <Icon name="check-double" size={12} color={colors.info} />;
			default:
				return <Icon name="clock" size={12} color="rgba(255, 255, 255, 0.7)" />;
		}
	};

	return (
		<View>
			{getIcon()}
		</View>
	);
};

export default MessageStatus;