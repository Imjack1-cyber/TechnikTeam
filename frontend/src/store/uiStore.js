import { create } from 'zustand';

// This store holds triggers for refetching data in response to SSE events.
// Components using the useApi hook can subscribe to these triggers.
export const useUIStore = create((set) => ({
  // A map of entity types to their last update timestamp.
  // The timestamp value itself is what triggers the re-render.
  refetchTriggers: {
    // User-facing entities
    ANNOUNCEMENT: null,
    EVENT: null,
    MEETING: null,
    STORAGE_ITEM: null,
    COURSE: null,
    KIT: null,
    FILE: null,
    CHANGELOG: null,
    CONVERSATION: null,
    EVENT_TASK: null,
    // Admin-facing entities
    USER: null,
    ADMIN_LOG: null,
    FEEDBACK: null,
    PROFILE_REQUEST: null,
    TRAINING_REQUEST: null,
    DAMAGE_REPORT: null,
    VENUE: null,
    EVENT_ROLE: null,
    EVENT_TASK_CATEGORY: null,
    MATRIX: null,
    TODO: null,
    ACHIEVEMENT: null,
    DOCUMENTATION: null,
    WIKI: null,
  },

  /**
   * Triggers a refetch for a specific entity type.
   * @param {string} entity - The uppercase entity type (e.g., 'EVENT', 'USER').
   */
  triggerRefetch: (entity) => {
    if (typeof entity !== 'string' || !entity) return;
    const upperEntity = entity.toUpperCase();
    set(state => {
      if (state.refetchTriggers.hasOwnProperty(upperEntity)) {
        return {
          refetchTriggers: {
            ...state.refetchTriggers,
            [upperEntity]: Date.now(),
          },
        };
      }
      console.warn(`Attempted to trigger refetch for unknown entity: ${upperEntity}`);
      return state;
    });
  },

  currentPageKey: null,
  setCurrentPageKey: (key) => set({ currentPageKey: key }),
}));