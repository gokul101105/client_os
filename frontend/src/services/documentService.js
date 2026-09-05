import api from './api';

export async function getDocuments(clientId) {
  const { data } = await api.get(`/clients/${clientId}/documents`);
  return data;
}

export async function uploadDocument(clientId, file) {
  const formData = new FormData();
  formData.append('file', file);
  // No explicit Content-Type header: the browser/axios sets
  // multipart/form-data with the correct boundary automatically when the
  // body is a FormData instance. Setting it manually is a common mistake —
  // it strips the boundary axios would have generated and the request fails.
  const { data } = await api.post(`/clients/${clientId}/documents`, formData);
  return data;
}

export async function deleteDocument(clientId, documentId) {
  await api.delete(`/clients/${clientId}/documents/${documentId}`);
}
