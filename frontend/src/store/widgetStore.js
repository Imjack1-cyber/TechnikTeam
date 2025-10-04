import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { storage } from '../lib/storage';

export const useWidgetStore = create(
    persist(
        (set) => ({
            // Data for widgets
            nextEvent: null,
            openTasks: [],
            latestAnnouncement: null,
            error: null,

            // Action to update all data at once
            setWidgetData: (data) => set({
                nextEvent: data.nextEvent,
                openTasks: data.openTasks,
                latestAnnouncement: data.latestAnnouncement,
                error: data.error,
            }),
        }),
        {
            name: 'widget-storage', // The key in AsyncStorage
            storage: createJSONStorage(() => storage),
        }
    )
);