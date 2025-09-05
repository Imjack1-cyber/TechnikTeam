import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { createStackNavigator } from '@react-navigation/stack';
import ConversationList from '../components/chat/ConversationList';
import MessageView from '../components/chat/MessageView';
import Icon from '@expo/vector-icons/FontAwesome5';

const Stack = createStackNavigator();

const ChatWelcomeScreen = ({ navigation }) => (
    <View style={styles.chatWelcomeView}>
        <Icon name="comments" size={60} color="#6c757d" />
        <Text style={styles.welcomeTitle}>Willkommen im Chat</Text>
        <Text style={styles.welcomeText}>Wähle ein Gespräch aus oder starte ein neues, um zu beginnen.</Text>
    </View>
);

// This component will act as a wrapper to decide which screen to show initially
const ChatHomeScreen = ({ navigation }) => {
    return <ConversationList navigation={navigation} />;
}

const ChatPage = () => {
	// In React Native, routing is handled by a Stack Navigator.
	// The ConversationList will navigate to the MessageView.
	// The concept of a single page with sliding panes is replaced by native stack navigation.
	return (
		<Stack.Navigator
            screenOptions={{
                headerShown: false
            }}
        >
			<Stack.Screen name="ConversationList" component={ChatHomeScreen} />
			<Stack.Screen name="MessageView" component={MessageView} />
		</Stack.Navigator>
	);
};

const styles = StyleSheet.create({
    chatWelcomeView: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#f8f9fa', // bg-color
        padding: 20,
    },
    welcomeTitle: {
        fontSize: 22,
        fontWeight: 'bold',
        marginTop: 16,
        color: '#002B5B', // heading-color
    },
    welcomeText: {
        fontSize: 16,
        color: '#6c757d', // text-muted-color
        textAlign: 'center',
        marginTop: 8,
    }
});


export default ChatPage;