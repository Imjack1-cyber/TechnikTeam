import { create } from 'zustand';
import * as Notifications from 'expo-notifications';

export const useDownloadStore = create((set, get) => ({
    /**
     * @type {Object.<string, {filename: string, status: 'starting'|'downloading'|'completed'|'error', progress: number, total: number, fileUri: string|null, nativeNotificationId: string|null, isLocallyComplete: boolean, autoRemoveTimeoutId?: number}>}
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
        set(state => {
            const existingDownload = state.downloads[id];
            if (!existingDownload) {
                // This can happen if a progress update arrives after the download was removed.
                return state;
            }
            
            // If a timeout is already set from a previous 'completed' update, clear it.
            if (existingDownload.autoRemoveTimeoutId) {
                clearTimeout(existingDownload.autoRemoveTimeoutId);
            }

            const updatedDownload = { ...existingDownload, ...updates };

            const isComplete = updatedDownload.status === 'completed' || (updatedDownload.progress >= updatedDownload.total && updatedDownload.total > 0);

            // Reconciliation Logic for native notifications
            if (isComplete && updatedDownload.nativeNotificationId) {
                console.log(`Dismissal condition met for download ${id}. Dismissing notification ${updatedDownload.nativeNotificationId}`);
                Notifications.dismissNotificationAsync(updatedDownload.nativeNotificationId);
                updatedDownload.status = 'completed';
                updatedDownload.nativeNotificationId = null; // Clear ID after dismissal
            }

            // Auto-remove the UI indicator after a delay when complete
            if (isComplete) {
                const timeoutId = setTimeout(() => {
                    get().removeDownload(id);
                }, 5000); // 5-second delay before removing from UI
                updatedDownload.autoRemoveTimeoutId = timeoutId;
            }

            return {
                downloads: {
                    ...state.downloads,
                    [id]: updatedDownload,
                },
            };
        });
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
        const downloadToRemove = state.downloads[id];
        if (downloadToRemove && downloadToRemove.autoRemoveTimeoutId) {
            clearTimeout(downloadToRemove.autoRemoveTimeoutId);
        }
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