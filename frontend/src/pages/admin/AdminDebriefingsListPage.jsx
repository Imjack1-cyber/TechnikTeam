import React, { useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import MarkdownDisplay from 'react-native-markdown-display';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';

const AdminDebriefingsListPage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/admin/events/debriefings'), []);
	const { data: debriefings, loading, error } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <View style={{flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start'}}>
                <TouchableOpacity onPress={() => navigation.navigate('AdminEventDebriefing', { eventId: item.eventId })}>
                    <Text style={styles.cardTitle}>{item.eventName}</Text>
                </TouchableOpacity>
                <Text style={styles.subtitle}>
                    {item.authorUsername} am {new Date(item.submittedAt).toLocaleDateString('de-DE')}
                </Text>
            </View>
            <View style={{marginTop: 16}}>
                <Text style={{fontWeight: 'bold', marginBottom: 4}}>Was lief gut?</Text>
                <MarkdownDisplay>{item.whatWentWell}</MarkdownDisplay>
            </View>
             <View style={{marginTop: 16}}>
                <Text style={{fontWeight: 'bold', marginBottom: 4}}>Was kann verbessert werden?</Text>
                <MarkdownDisplay>{item.whatToImprove}</MarkdownDisplay>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
			<View style={{flexDirection: 'row', alignItems: 'center', padding: 16}}>
                <Icon name="clipboard-check" size={24} style={{color: getThemeColors(theme).heading, marginRight: 12}} />
			    <Text style={styles.title}>Event-Debriefings</Text>
            </View>
			<Text style={styles.subtitle}>Eine Übersicht aller nachbereiteten Veranstaltungen zur Analyse und Verbesserung.</Text>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={debriefings}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
                ListEmptyComponent={<View style={styles.card}><Text>Es wurden noch keine Debriefings eingereicht.</Text></View>}
            />
		</View>
	);
};

export default AdminDebriefingsListPage;