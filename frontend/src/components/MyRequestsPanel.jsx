import { useEffect, useState } from 'react';
import { getMyRequests } from '../services/clientRequestService';
import RequestStatusBadge from './RequestStatusBadge';

function formatDate(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString();
}

function describe(request) {
  if (request.type === 'CREATE') {
    return `Create "${request.snapshotName}"`;
  }
  return `Delete client #${request.targetClientId ?? '—'}`;
}

// refreshKey is bumped by the parent after a new request is submitted, so
// this list picks it up without polling.
export default function MyRequestsPanel({ refreshKey }) {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getMyRequests()
      .then((data) => {
        if (!cancelled) setRequests(data);
      })
      .catch(() => {
        if (!cancelled) setError('Could not load your requests.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [refreshKey]);

  if (loading) return null;
  if (error) return <p className="mt-8 text-sm text-red-600">{error}</p>;
  if (requests.length === 0) return null;

  return (
    <div className="mt-10">
      <h2 className="text-sm font-semibold text-gray-900">My requests</h2>
      <ul className="mt-3 divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
        {requests.map((request) => (
          <li key={request.id} className="flex items-center justify-between gap-4 px-4 py-3">
            <div>
              <p className="text-sm font-medium text-gray-900">{describe(request)}</p>
              <p className="text-xs text-gray-500">Requested {formatDate(request.requestedAt)}</p>
            </div>
            <RequestStatusBadge status={request.status} />
          </li>
        ))}
      </ul>
    </div>
  );
}
