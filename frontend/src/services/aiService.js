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
