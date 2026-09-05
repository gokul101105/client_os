import api from './api';

export async function getClients() {
  const { data } = await api.get('/clients');
  return data;
}

export async function getClientById(id) {
  const { data } = await api.get(`/clients/${id}`);
  return data;
}
