import { create } from 'zustand';

export const useUIStore = create((set) => ({
  currentPageKey: null,
  setCurrentPageKey: (key) => set({ currentPageKey: key }),
}));