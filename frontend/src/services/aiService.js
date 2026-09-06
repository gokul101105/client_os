import api from './api';

// Longer than Spring Boot's own ai-service.timeout-seconds (30s), so the
// backend's timeout fires first and returns a clean error message instead
// of axios giving up on a request that might still be about to succeed.
const CHAT_TIMEOUT_MS = 35000;

export async function askQuestion(clientId, message) {
  const { data } = await api.post(
    `/clients/${clientId}/ai/chat`,
    { message },
    { timeout: CHAT_TIMEOUT_MS }
  );
  return data; // { reply, sourceChunkIds }
}

// Summary generation involves broader retrieval than a single chat
// question, so it gets the same generous timeout.
const SUMMARY_TIMEOUT_MS = 35000;

export async function generateClientSummary(clientId) {
  const { data } = await api.post(`/clients/${clientId}/ai/summary`, null, {
    timeout: SUMMARY_TIMEOUT_MS,
  });
  return data;
}

export async function getLatestClientSummary(clientId) {
  const { data } = await api.get(`/clients/${clientId}/ai/summary`);
  return data;
}

// Recommendations run a retrieval step plus a Claude call, same class of
// latency as summary generation.
const RECOMMEND_TIMEOUT_MS = 35000;

export async function getRecommendations(clientId) {
  const { data } = await api.post(`/clients/${clientId}/ai/recommendations`, null, {
    timeout: RECOMMEND_TIMEOUT_MS,
  });
  return data; // { recommendations: [{ action, reason, priority }] }
}

// The agent runs multiple gathering steps before its one Claude call, so
// it gets the same generous timeout as the other multi-step features.
const MEETING_BRIEF_TIMEOUT_MS = 35000;

export async function getMeetingBrief(clientId) {
  const { data } = await api.post(`/clients/${clientId}/ai/meeting-brief`, null, {
    timeout: MEETING_BRIEF_TIMEOUT_MS,
  });
  return data;
}
