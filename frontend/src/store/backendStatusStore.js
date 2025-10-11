import { create } from 'zustand';

export const useBackendStatusStore = create((set) => ({
  isBackendDown: false,
  setIsBackendDown: (status) => set({ isBackendDown: status }),
}));