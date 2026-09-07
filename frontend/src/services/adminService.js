import api from './api';

export async function createAccountManager({ name, email, password }) {
  const { data } = await api.post('/admin/users', { name, email, password });
  return data;
}

export async function getUsers() {
  const { data } = await api.get('/admin/users');
  return data;
}

export async function deleteUser(userId) {
  await api.delete(`/admin/users/${userId}`);
}

export async function getRequests(status) {
  const { data } = await api.get('/admin/requests', {
    params: status ? { status } : undefined,
  });
  return data;
}

export async function approveRequest(requestId, decisionNote) {
  const { data } = await api.post(`/admin/requests/${requestId}/approve`, {
    decisionNote: decisionNote || null,
  });
  return data;
}

export async function rejectRequest(requestId, decisionNote) {
  const { data } = await api.post(`/admin/requests/${requestId}/reject`, {
    decisionNote: decisionNote || null,
  });
  return data;
}
