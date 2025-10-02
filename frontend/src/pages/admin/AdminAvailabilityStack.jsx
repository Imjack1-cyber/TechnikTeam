import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';
import Header from '../../components/layout/Header';

import AdminAvailabilityPage from './AdminAvailabilityPage';
import AdminAvailabilityPollDetailsPage from './AdminAvailabilityPollDetailsPage';

const Stack = createStackNavigator();

const AdminAvailabilityStack = () => {
    return (
        <Stack.Navigator screenOptions={{ header: (props) => <Header {...props} /> }}>
            <Stack.Screen name="AdminAvailabilityList" component={AdminAvailabilityPage} options={{ title: 'Verfügbarkeits-Check' }} />
            <Stack.Screen name="AdminAvailabilityPollDetails" component={AdminAvailabilityPollDetailsPage} options={{ title: 'Umfrage-Ergebnisse' }} />
        </Stack.Navigator>
    );
};

export default AdminAvailabilityStack;