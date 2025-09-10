import { create } from 'zustand';
import * as Notifications from 'expo-notifications';

export const useDownloadStore = create((set, get) => ({
    /**
     * @type {Object.<string, {filename: string, status: 'starting'|'downloading'|'completed'|'error', progress: number, total: number, fileUri: string|null, nativeNotificationId: string|null, isLocallyComplete: boolean}>}
     */
    downloads: {},

    addDownload: (id, filename) => set(state => ({
        downloads: {
            ...state.downloads,
            [id]: {
                filename,
                status: 'starting',
                progress: 0,
                total: 1, // Start with 1 to avoid division by zero
                fileUri: null,
                nativeNotificationId: null,
                isLocallyComplete: false, // Track if the local download promise has resolved
            },
        },
    })),

    updateDownload: (id, updates) => {
        const state = get();
        const existingDownload = state.downloads[id];

        const newDownloadState = {
            ...state.downloads,
            [id]: { ...(existingDownload || {
                filename: 'Unbekannter Download',
                status: 'downloading',
                progress: 0,
                total: 1,
                fileUri: null,
                nativeNotificationId: null,
                isLocallyComplete: false,
            }), ...updates },
        };
        
        const updatedDownload = newDownloadState[id];
        const isComplete = updatedDownload.status === 'completed' || (updatedDownload.progress >= updatedDownload.total && updatedDownload.total > 0);

        // Reconciliation Logic: Dismiss the native notification if the download is complete
        // AND we have the notification's ID. This handles all cases (foreground and background).
        if (isComplete && updatedDownload.nativeNotificationId) {
            console.log(`Dismissal condition met for download ${id}. Dismissing notification ${updatedDownload.nativeNotificationId}`);
            Notifications.dismissNotificationAsync(updatedDownload.nativeNotificationId);
            updatedDownload.status = 'completed';
            updatedDownload.nativeNotificationId = null; // Clear ID after dismissal
        }

        set({ downloads: newDownloadState });
    },

    markDownloadAsLocallyComplete: (id) => {
        // This action signals that the FileSystem.downloadAsync promise has resolved.
        // It updates the status, which then triggers the reconciliation logic in updateDownload.
        const state = get();
        const download = state.downloads[id];
        if (download) {
            get().updateDownload(id, { isLocallyComplete: true, status: 'completed' });
        }
    },

    removeDownload: (id) => set(state => {
        const newDownloads = { ...state.downloads };
        delete newDownloads[id];
        return { downloads: newDownloads };
    }),

    clearFinishedDownloads: () => set(state => {
        const activeDownloads = {};
        for (const id in state.downloads) {
            if (state.downloads[id].status === 'downloading' || state.downloads[id].status === 'starting') {
                activeDownloads[id] = state.downloads[id];
            }
        }
        return { downloads: activeDownloads };
    }),
}));