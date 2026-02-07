import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { GraphNodeResponse, GraphRequest } from '@/services/graph';

export interface SessionRuntimeState {
  isStreaming: boolean;
  nodeBlocks: GraphNodeResponse[][];
  closeStream: (() => void) | null;
  lastRequest: GraphRequest | null;
  htmlReportContent: string;
  htmlReportSize: number;
  markdownReportContent: string;
}

export const useSessionStateStore = defineStore('sessionState', () => {
  const sessionStates = ref<Map<string, SessionRuntimeState>>(new Map());

  const getSessionState = (sessionId: string): SessionRuntimeState => {
    if (!sessionStates.value.has(sessionId)) {
      sessionStates.value.set(sessionId, {
        isStreaming: false,
        nodeBlocks: [],
        closeStream: null,
        lastRequest: null,
        htmlReportContent: '',
        htmlReportSize: 0,
        markdownReportContent: '',
      });
    }
    return sessionStates.value.get(sessionId)!;
  };

  const deleteSessionState = (sessionId: string) => {
    const state = sessionStates.value.get(sessionId);
    if (state?.closeStream) {
      state.closeStream();
    }
    sessionStates.value.delete(sessionId);
  };
  
  const getRunningSessionIds = (): string[] => {
     const runningIds: string[] = [];
     sessionStates.value.forEach((state, sessionId) => {
       if (state.isStreaming) {
         runningIds.push(sessionId);
       }
     });
     return runningIds;
  };

  return {
    sessionStates,
    getSessionState,
    deleteSessionState,
    getRunningSessionIds
  };
});
