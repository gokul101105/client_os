import { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import RequestStatusBadge from '../components/RequestStatusBadge';
import { approveRequest, getRequests, rejectRequest } from '../services/adminService';

function formatDate(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString();
}

function describe(request) {
  if (request.type === 'CREATE') {
    const parts = [request.snapshotIndustry, request.snapshotPlan].filter(Boolean).join(' · ');
    return `Create "${request.snapshotName}"${parts ? ` (${parts})` : ''}`;
  }
  return `Delete client #${request.targetClientId ?? '—'}`;
}

const STATUS_OPTIONS = ['PENDING', 'APPROVED', 'REJECTED', 'ALL'];

export default function AdminRequestsPage() {
  const [statusFilter, setStatusFilter] = useState('PENDING');
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // { id, action } while a row is asking for confirmation before deciding.
  const [confirming, setConfirming] = useState(null);
  const [decisionNote, setDecisionNote] = useState('');
  const [deciding, setDeciding] = useState(false);
  const [rowError, setRowError] = useState('');

  const loadRequests = () => {
    setLoading(true);
    setError('');
    getRequests(statusFilter === 'ALL' ? undefined : statusFilter)
      .then(setRequests)
      .catch(() => setError('Could not load requests.'))
      .finally(() => setLoading(false));
  };

  useEffect(loadRequests, [statusFilter]);

  const startConfirm = (id, action) => {
    setConfirming({ id, action });
    setDecisionNote('');
    setRowError('');
  };

  const cancelConfirm = () => {
    setConfirming(null);
    setDecisionNote('');
    setRowError('');
  };

  const decide = async () => {
    if (!confirming) return;
    setDeciding(true);
    setRowError('');
    try {
      const decide = confirming.action === 'approve' ? approveRequest : rejectRequest;
      await decide(confirming.id, decisionNote);
      // Decided requests drop out of this queue -- the admin's view is a
      // to-do list, not a history log (full history stays visible to the
      // requester via "My requests", and via the status filter here).
      setRequests((prev) => prev.filter((r) => r.id !== confirming.id));
      setConfirming(null);
      setDecisionNote('');
    } catch (err) {
      setRowError(err.response?.data?.message || 'Could not record this decision.');
    } finally {
      setDeciding(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-6xl px-6 py-8">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-semibold text-gray-900">Requests</h1>
            <p className="mt-1 text-sm text-gray-500">
              {requests.length} request{requests.length === 1 ? '' : 's'}
            </p>
          </div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="rounded-md border border-gray-300 px-3 py-1.5 text-sm text-black focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          >
            {STATUS_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option === 'ALL' ? 'All statuses' : option}
              </option>
            ))}
          </select>
        </div>

        {loading && <p className="mt-8 text-sm text-gray-500">Loading requests…</p>}
        {error && <p className="mt-8 text-sm text-red-600">{error}</p>}

        {!loading && !error && requests.length === 0 && (
          <p className="mt-8 text-sm text-gray-500">No requests here.</p>
        )}

        {!loading && !error && requests.length > 0 && (
          <ul className="mt-6 divide-y divide-gray-200 rounded-lg border border-gray-200 bg-white">
            {requests.map((request) => (
              <li key={request.id} className="px-4 py-4">
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <p className="text-sm font-medium text-gray-900">{describe(request)}</p>
                    <p className="mt-1 text-xs text-gray-500">
                      Requested by {request.requestedByName} · {formatDate(request.requestedAt)}
                    </p>
                    {request.status !== 'PENDING' && (
                      <p className="mt-1 text-xs text-gray-500">
                        {request.status === 'APPROVED' ? 'Approved' : 'Rejected'} by {request.decidedByName} ·{' '}
                        {formatDate(request.decidedAt)}
                        {request.decisionNote && ` — "${request.decisionNote}"`}
                      </p>
                    )}
                  </div>
                  <div className="flex shrink-0 items-center gap-2">
                    <RequestStatusBadge status={request.status} />
                    {request.status === 'PENDING' && confirming?.id !== request.id && (
                      <>
                        <button
                          type="button"
                          onClick={() => startConfirm(request.id, 'approve')}
                          className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700"
                        >
                          Accept
                        </button>
                        <button
                          type="button"
                          onClick={() => startConfirm(request.id, 'reject')}
                          className="rounded-md border border-red-300 px-3 py-1.5 text-sm font-medium text-red-600 hover:bg-red-50"
                        >
                          Reject
                        </button>
                      </>
                    )}
                  </div>
                </div>

                {confirming?.id === request.id && (
                  <div className="mt-3 rounded-md border border-gray-200 bg-gray-50 p-3">
                    <p className="text-sm text-gray-700">
                      {confirming.action === 'approve'
                        ? request.type === 'CREATE'
                          ? 'This will create the client.'
                          : 'This will permanently delete the client.'
                        : 'This will reject the request. No data changes.'}
                    </p>
                    <label htmlFor={`note-${request.id}`} className="mt-2 block text-xs font-medium text-gray-600">
                      Note (optional)
                    </label>
                    <input
                      id={`note-${request.id}`}
                      type="text"
                      value={decisionNote}
                      onChange={(e) => setDecisionNote(e.target.value)}
                      className="mt-1 w-full rounded-md border border-gray-300 px-3 py-1.5 text-sm text-black focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                    />
                    {rowError && <p className="mt-2 text-sm text-red-600">{rowError}</p>}
                    <div className="mt-3 flex justify-end gap-2">
                      <button
                        type="button"
                        onClick={cancelConfirm}
                        disabled={deciding}
                        className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-60"
                      >
                        Cancel
                      </button>
                      <button
                        type="button"
                        onClick={decide}
                        disabled={deciding}
                        className={`rounded-md px-3 py-1.5 text-sm font-medium text-white disabled:opacity-60 ${
                          confirming.action === 'approve'
                            ? 'bg-blue-600 hover:bg-blue-700'
                            : 'bg-red-600 hover:bg-red-700'
                        }`}
                      >
                        {deciding
                          ? 'Confirming…'
                          : confirming.action === 'approve'
                            ? 'Confirm accept'
                            : 'Confirm reject'}
                      </button>
                    </div>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}
      </main>
    </div>
  );
}
