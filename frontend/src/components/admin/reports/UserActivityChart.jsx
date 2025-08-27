import React from 'react';
import { View, Text, Dimensions, StyleSheet } from 'react-native';
import { BarChart } from 'react-native-chart-kit';
import { useAuthStore } from '../../../store/authStore';
import { getThemeColors } from '../../../styles/theme';

const UserActivityChart = ({ activityData }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);

	if (!activityData || activityData.length === 0) {
		return <Text style={{color: colors.textMuted}}>Nicht genügend Daten für Benutzeraktivität vorhanden.</Text>;
	}

    // react-native-chart-kit expects labels and data separately
	const chartData = {
		labels: activityData.map(d => d.username.substring(0, 8)), // Shorten labels
		datasets: [
			{
				data: activityData.map(d => d.participation_count),
			},
		],
	};
    
    const chartConfig = {
        backgroundColor: colors.surface,
        backgroundGradientFrom: colors.surface,
        backgroundGradientTo: colors.surface,
        decimalPlaces: 0,
        color: (opacity = 1) => `rgba(0, 123, 255, ${opacity})`, // primary-color
        labelColor: (opacity = 1) => colors.textMuted,
        style: { borderRadius: 8 },
        barPercentage: 0.5,
    };

	return (
		<View style={styles.container}>
            <Text style={{color: colors.heading, fontWeight: 'bold', marginBottom: 8}}>Event-Teilnahmen pro Benutzer</Text>
			<BarChart
				data={chartData}
				width={Dimensions.get('window').width - 64}
				height={400}
				yAxisLabel=""
                yAxisSuffix=""
				chartConfig={chartConfig}
                fromZero={true}
				verticalLabelRotation={30}
			/>
		</View>
	);
};

const styles = StyleSheet.create({
    container: {
        alignItems: 'center',
    }
});

export default UserActivityChart;