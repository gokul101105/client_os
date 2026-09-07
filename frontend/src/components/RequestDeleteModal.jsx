import { useState } from 'react';
import { submitDeleteRequest } from '../services/clientRequestService';

export default function RequestDeleteModal({ clientId, clientName, onClose, onSubmitted }) {
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleConfirm = async () => {
    setError('');
    setSubmitting(true);
    try {
      const request = await submitDeleteRequest(clientId);
      onSubmitted(request);
    } catch (err) {
      const message = err.response?.data?.message || 'Could not submit this request.';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <div className="w-full max-w-sm rounded-xl border border-gray-200 bg-white p-6 shadow-lg">
        <h2 className="text-lg font-semibold text-gray-900">Request deletion</h2>
        <p className="mt-2 text-sm text-gray-600">
          This submits a request for an admin to delete <span className="font-medium">{clientName}</span>.
          Nothing is deleted until it's approved.
        </p>

        {error && <p className="mt-3 text-sm text-red-600">{error}</p>}

        <div className="mt-5 flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            disabled={submitting}
            className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-60"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            disabled={submitting}
            className="rounded-md bg-red-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-60"
          >
            {submitting ? 'Submitting…' : 'Request deletion'}
          </button>
        </div>
      </div>
    </div>
  );
}
