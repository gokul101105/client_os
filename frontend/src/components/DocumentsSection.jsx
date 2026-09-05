import { useEffect, useState } from 'react';
import { deleteDocument, getDocuments, uploadDocument } from '../services/documentService';

const ALLOWED_EXTENSIONS = ['pdf', 'txt', 'docx'];
const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

function formatDate(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString();
}

// Client-side validation is purely a fast-feedback UX nicety — the backend
// re-validates everything (extension, size) and is the actual authority.
function validateFile(file) {
  const extension = file.name.split('.').pop()?.toLowerCase();
  if (!extension || !ALLOWED_EXTENSIONS.includes(extension)) {
    return 'Only PDF, TXT, and DOCX files are allowed.';
  }
  if (file.size > MAX_FILE_SIZE_BYTES) {
    return 'File must be 10MB or smaller.';
  }
  return null;
}

export default function DocumentsSection({ clientId }) {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    getDocuments(clientId)
      .then((data) => {
        if (!cancelled) setDocuments(data);
      })
      .catch(() => {
        if (!cancelled) setError('Could not load documents.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [clientId]);

  const refresh = () => {
    getDocuments(clientId)
      .then(setDocuments)
      .catch(() => setError('Could not load documents.'));
  };

  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    e.target.value = ''; // allow re-selecting the same file again later
    if (!file || uploading) return;

    const validationError = validateFile(file);
    if (validationError) {
      setUploadError(validationError);
      return;
    }

    setUploadError('');
    setUploading(true);
    try {
      await uploadDocument(clientId, file);
      refresh();
    } catch (err) {
      setUploadError(err.response?.data?.message || 'Upload failed.');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (documentId) => {
    if (!window.confirm('Delete this document?')) return;
    try {
      await deleteDocument(clientId, documentId);
      setDocuments((prev) => prev.filter((doc) => doc.id !== documentId));
    } catch {
      setError('Could not delete this document.');
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-medium text-gray-700">
          {documents.length} document{documents.length === 1 ? '' : 's'}
        </h2>
        <label
          htmlFor="document-upload-input"
          className={`rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 ${
            uploading ? 'pointer-events-none opacity-60' : 'cursor-pointer'
          }`}
        >
          {uploading ? 'Uploading…' : 'Upload document'}
        </label>
        <input
          id="document-upload-input"
          type="file"
          accept=".pdf,.txt,.docx"
          onChange={handleFileChange}
          disabled={uploading}
          className="hidden"
        />
      </div>

      {uploadError && <p className="mt-2 text-sm text-red-600">{uploadError}</p>}

      {loading && <p className="mt-6 text-sm text-gray-500">Loading documents…</p>}
      {error && <p className="mt-6 text-sm text-red-600">{error}</p>}

      {!loading && !error && documents.length === 0 && (
        <p className="mt-6 text-sm text-gray-500">No documents uploaded yet.</p>
      )}

      {!loading && !error && documents.length > 0 && (
        <ul className="mt-4 divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
          {documents.map((doc) => (
            <li key={doc.id} className="flex items-center justify-between px-4 py-3">
              <div>
                <p className="text-sm font-medium text-gray-900">{doc.fileName}</p>
                <p className="text-xs text-gray-500">
                  {doc.fileType} • {doc.uploadedByName} • {formatDate(doc.uploadedAt)}
                </p>
              </div>
              <button
                type="button"
                onClick={() => handleDelete(doc.id)}
                className="text-sm text-red-600 hover:underline"
              >
                Delete
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
