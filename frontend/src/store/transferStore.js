import { create } from 'zustand';
import * as Notifications from 'expo-notifications';

export const useTransferStore = create((set, get) => ({
    /**
     * @type {Object.<string, {filename: string, type: 'download' | 'upload', status: 'starting'|'progressing'|'completed'|'error'|'canceled', progress: number, total: number, speed: number, eta: number, fileUri: string|null, nativeNotificationId: string|null, source: any|null, displayMode: 'indicator' | 'button'}>}
     */
    transfers: {},

    getTransfer: (id) => get().transfers[id],

    addTransfer: (id, filename, type, totalSize) => set(state => ({
        transfers: {
            ...state.transfers,
            [id]: {
                filename,
                type,
                status: 'starting',
                progress: 0,
                total: totalSize || 1,
                speed: 0,
                eta: Infinity,
                fileUri: null,
                nativeNotificationId: null,
                source: null, // To store AbortController or XHR instance
                displayMode: 'indicator', // Default display mode
            },
        },
    })),

    updateTransfer: (id, updates) => {
        set(state => {
            const existingTransfer = state.transfers[id];
            if (!existingTransfer) {
                return state;
            }
            
            const updatedTransfer = { ...existingTransfer, ...updates };

            if (updatedTransfer.status === 'completed' || updatedTransfer.status === 'error' || updatedTransfer.status === 'canceled') {
                // Auto-remove after a delay
                setTimeout(() => get().removeTransfer(id), 5000);
            }

            return {
                transfers: {
                    ...state.transfers,
                    [id]: updatedTransfer,
                },
            };
        });
    },

    setTransferDisplayMode: (id, mode) => {
        set(state => {
            const existingTransfer = state.transfers[id];
            if (!existingTransfer) {
                return state;
            }
            return {
                transfers: {
                    ...state.transfers,
                    [id]: { ...existingTransfer, displayMode: mode },
                },
            };
        });
    },
    
    cancelTransfer: (id) => {
        const transfer = get().transfers[id];
        if (transfer && transfer.source) {
            transfer.source.abort(); // This works for both AbortController and XHR
            get().updateTransfer(id, { status: 'canceled' });
        }
    },

    removeTransfer: (id) => set(state => {
        const newTransfers = { ...state.transfers };
        delete newTransfers[id];
        return { transfers: newTransfers };
    }),

    clearFinished: () => set(state => {
        const activeTransfers = {};
        for (const id in state.transfers) {
            if (state.transfers[id].status === 'progressing' || state.transfers[id].status === 'starting') {
                activeTransfers[id] = state.transfers[id];
            }
        }
        return { transfers: activeTransfers };
    }),
}));