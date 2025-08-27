import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

const StatusBadge = ({ status }) => {
	const getStatusStyle = () => {
		const upperStatus = status?.toUpperCase() || '';
		switch (upperStatus) {
			case 'LAUFEND':
			case 'PENDING':
			case 'VIEWED':
			case 'PLANNED':
				return styles.warn;
			case 'GEPLANT':
			case 'KOMPLETT':
			case 'ERLEDIGT':
			case 'APPROVED':
			case 'NEW':
				return styles.ok;
			case 'ABGESCHLOSSEN':
			case 'ABGESAGT':
			case 'REJECTED':
			case 'COMPLETED':
				return styles.info;
			default:
				return styles.info;
		}
	};
	
	const getStatusTextStyle = () => {
		const upperStatus = status?.toUpperCase() || '';
		switch (upperStatus) {
			case 'LAUFEND':
			case 'PENDING':
			case 'VIEWED':
			case 'PLANNED':
				return styles.darkText;
			default:
				return styles.lightText;
		}
	};

	return (
        <View style={[styles.badge, getStatusStyle()]}>
            <Text style={[styles.badgeText, getStatusTextStyle()]}>{status}</Text>
        </View>
    );
};

const styles = StyleSheet.create({
    badge: {
        paddingVertical: 4,
        paddingHorizontal: 10,
        borderRadius: 20,
    },
    badgeText: {
        fontSize: 12,
        fontWeight: '600',
        textTransform: 'uppercase',
    },
    ok: {
        backgroundColor: '#28a745', // success-color
    },
    warn: {
        backgroundColor: '#ffc107', // warning-color
    },
    danger: {
        backgroundColor: '#dc3545', // danger-color
    },
    info: {
        backgroundColor: '#6c757d', // text-muted-color
    },
	lightText: {
		color: '#fff',
	},
	darkText: {
		color: '#000',
	}
});


export default StatusBadge;