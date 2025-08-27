// Platform-specific implementation for Native (using AsyncStorage)
import AsyncStorage from '@react-native-async-storage/async-storage';

const AUTH_TOKEN_KEY = 'technikteam-auth-token';

export const storage = {
  setItem: async (key, value) => {
    try {
      await AsyncStorage.setItem(key, value);
    } catch (e) {
      console.error('Failed to save to AsyncStorage', e);
    }
  },
  getItem: async (key) => {
    try {
      return await AsyncStorage.getItem(key);
    } catch (e) {
      console.error('Failed to get from AsyncStorage', e);
      return null;
    }
  },
  removeItem: async (key) => {
    try {
      await AsyncStorage.removeItem(key);
    } catch (e) {
      console.error('Failed to remove from AsyncStorage', e);
    }
  },
};

export const getToken = () => storage.getItem(AUTH_TOKEN_KEY);
export const setToken = (token) => storage.setItem(AUTH_TOKEN_KEY, token);
export const removeToken = () => storage.removeItem(AUTH_TOKEN_KEY);