import api from './api';

export async function submitCreateRequest({ name, industry, plan }) {
  const { data } = await api.post('/clients/requests', { name, industry, plan });
  return data;
}

export async function submitDeleteRequest(clientId) {
  const { data } = await api.post(`/clients/${clientId}/delete-requests`);
  return data;
}

export async function getMyRequests() {
  const { data } = await api.get('/clients/requests/mine');
  return data;
}
