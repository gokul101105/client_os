import api from './api';

// Plain Spring Boot business logic (Module 14) -- no AI service involved,
// so no extended timeout needed here (unlike aiService.js's calls).
export async function getLatestHealth(clientId) {
  const { data } = await api.get(`/clients/${clientId}/health`);
  return data;
}

export async function recomputeHealth(clientId) {
  const { data } = await api.post(`/clients/${clientId}/health/recompute`);
  return data;
}
