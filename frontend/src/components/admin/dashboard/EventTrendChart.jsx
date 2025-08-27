import React from 'react';
import { View, Text, Dimensions, StyleSheet } from 'react-native';
import { LineChart } from 'react-native-chart-kit';
import { useAuthStore } from '../../../store/authStore';
import { getThemeColors } from '../../../styles/theme';

const EventTrendChart = ({ trendData }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);

	if (!trendData || trendData.length === 0) {
		return <Text style={{color: colors.textMuted}}>Nicht genügend Daten für den Event-Trend vorhanden.</Text>;
	}

	const chartData = {
		labels: trendData.map(d => d.month.substring(5)), // e.g., '01', '02'
		datasets: [
			{
				data: trendData.map(d => d.count),
			},
		],
	};

    const chartConfig = {
        backgroundColor: colors.surface,
        backgroundGradientFrom: colors.surface,
        backgroundGradientTo: colors.surface,
        decimalPlaces: 0,
        color: (opacity = 1) => `rgba(0, 123, 255, ${opacity})`, // primary-color
        labelColor: (opacity = 1) => `rgba(108, 117, 125, ${opacity})`, // text-muted-color
        style: {
          borderRadius: 8,
        },
        propsForDots: {
          r: '4',
          strokeWidth: '2',
          stroke: colors.primaryHover,
        },
      };

	return (
		<View style={styles.container}>
			<LineChart
				data={chartData}
				width={Dimensions.get('window').width - 64} // screen width - padding
				height={250}
				yAxisLabel=""
				yAxisSuffix=""
                yAxisInterval={1}
				chartConfig={chartConfig}
				bezier
				style={{
					borderRadius: 8,
				}}
			/>
		</View>
	);
};

const styles = StyleSheet.create({
    container: {
        alignItems: 'center',
    }
});

export default EventTrendChart;