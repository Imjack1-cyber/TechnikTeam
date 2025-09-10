import { useState, useEffect, useRef } from 'react';
import { Platform } from 'react-native';
import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import { useDownloadStore } from '../store/downloadStore';

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
    // Create notification channels for Android
    await Notifications.setNotificationChannelAsync('default', {
      name: 'Default',
      importance: Notifications.AndroidImportance.DEFAULT,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#FF231F7C',
    });
    await Notifications.setNotificationChannelAsync('downloads', {
        name: 'Downloads',
        importance: Notifications.AndroidImportance.LOW, // For progress, so it's less intrusive
    });
     await Notifications.setNotificationChannelAsync('reminders', {
        name: 'Reminders',
        importance: Notifications.AndroidImportance.HIGH,
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
    token = (await Notifications.getDevicePushTokenAsync()).data;
    console.log('Native FCM Token:', token);
  } else {
    console.log('Must use physical device for Push Notifications');
  }

  return token;
}


export const usePushNotifications = () => {
    const isAuthenticated = useAuthStore(state => state.isAuthenticated);

    useEffect(() => {
        // Web push notifications require VAPID setup.
        // We will only initialize native push notifications.
        if (Platform.OS === 'web' || !isAuthenticated) {
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
            const { data } = notification.request.content;
            const identifier = notification.request.identifier;
            
            // Check for progress data
            if (data && data.progressMax) {
                const downloadId = data.downloadId || 'unknown';
                const progressCurrent = parseInt(data.progressCurrent, 10);
                const progressMax = parseInt(data.progressMax, 10);
                const isComplete = progressCurrent >= progressMax;

                // Centralize all update logic in the store. The store will handle dismissal.
                useDownloadStore.getState().updateDownload(downloadId, {
                    progress: progressCurrent,
                    total: progressMax,
                    status: isComplete ? 'completed' : 'downloading',
                    nativeNotificationId: identifier, // Always pass the ID
                });
            }
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