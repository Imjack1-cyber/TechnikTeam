import { useState, useEffect, useRef } from 'react';
import { Platform } from 'react-native';
import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
});

async function registerForPushNotificationsAsync() {
  let token;

  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('default', {
      name: 'default',
      importance: Notifications.AndroidImportance.MAX,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#FF231F7C',
    });
  }

  if (Device.isDevice) {
    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;
    if (existingStatus !== 'granted') {
      const { status } = await Notifications.requestPermissionsAsync();
      finalStatus = status;
    }
    if (finalStatus !== 'granted') {
      console.log('Failed to get push token for push notification!');
      return;
    }
    // Learn more about projectId: https://docs.expo.dev/push-notifications/push-notifications-setup/#configure-projectid
    // Must be set in app.json
    token = (await Notifications.getExpoPushTokenAsync()).data;
    console.log('Expo Push Token:', token);
  } else {
    console.log('Must use physical device for Push Notifications');
  }

  return token;
}


export const usePushNotifications = () => {
    const isAuthenticated = useAuthStore(state => state.isAuthenticated);

    useEffect(() => {
        if (!isAuthenticated) {
            return;
        }

        const setupNotifications = async () => {
            const token = await registerForPushNotificationsAsync();
            if (token) {
                try {
                    await apiClient.post('/public/profile/register-device', { token });
                    console.log('Successfully registered device token with backend.');
                } catch (error) {
                    console.error('Failed to register device token with backend:', error);
                }
            }
        };

        setupNotifications();

        const notificationListener = Notifications.addNotificationReceivedListener(notification => {
            console.log('Notification received while app is in foreground:', notification);
        });

        const responseListener = Notifications.addNotificationResponseReceivedListener(response => {
            console.log('User tapped on notification:', response);
            // Handle navigation here if needed based on response.notification.request.content.data
        });

        return () => {
            Notifications.removeNotificationSubscription(notificationListener);
            Notifications.removeNotificationSubscription(responseListener);
        };
    }, [isAuthenticated]);
};