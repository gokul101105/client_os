import { useState } from 'react';
import { submitCreateRequest } from '../services/clientRequestService';

export default function AddClientModal({ onClose, onSubmitted }) {
  const [name, setName] = useState('');
  const [industry, setIndustry] = useState('');
  const [plan, setPlan] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const request = await submitCreateRequest({ name, industry, plan });
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
        <h2 className="text-lg font-semibold text-gray-900">Request new client</h2>
        <p className="mt-1 text-sm text-gray-500">
          This submits a request for an admin to review — the client isn't created until it's approved.
        </p>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label htmlFor="client-name" className="block text-sm font-medium text-gray-700">
              Name
            </label>
            <input
              id="client-name"
              type="text"
              required
              maxLength={255}
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm text-black focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>
          <div>
            <label htmlFor="client-industry" className="block text-sm font-medium text-gray-700">
              Industry
            </label>
            <input
              id="client-industry"
              type="text"
              maxLength={100}
              value={industry}
              onChange={(e) => setIndustry(e.target.value)}
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm text-black focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>
          <div>
            <label htmlFor="client-plan" className="block text-sm font-medium text-gray-700">
              Plan
            </label>
            <input
              id="client-plan"
              type="text"
              maxLength={50}
              value={plan}
              onChange={(e) => setPlan(e.target.value)}
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm text-black focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>

          {error && <p className="text-sm text-red-600">{error}</p>}

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-60"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
            >
              {submitting ? 'Submitting…' : 'Submit request'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
