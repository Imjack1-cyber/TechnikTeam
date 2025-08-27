// Platform-specific implementation for Web (using localStorage)

const AUTH_TOKEN_KEY = 'technikteam-auth-token';

export const storage = {
  setItem: async (key, value) => {
    try {
      localStorage.setItem(key, value);
    } catch (e) {
      console.error('Failed to save to localStorage', e);
    }
  },
  getItem: async (key) => {
    try {
      return localStorage.getItem(key);
    } catch (e) {
      console.error('Failed to get from localStorage', e);
      return null;
    }
  },
  removeItem: async (key) => {
    try {
      localStorage.removeItem(key);
    } catch (e) {
      console.error('Failed to remove from localStorage', e);
    }
  },
};

export const getToken = () => storage.getItem(AUTH_TOKEN_KEY);
export const setToken = (token) => storage.setItem(AUTH_TOKEN_KEY, token);
export const removeToken = () => storage.removeItem(AUTH_TOKEN_KEY);