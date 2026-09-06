import { useEffect, useState } from 'react';
import { getLatestHealth, recomputeHealth } from '../services/healthService';

function formatDate(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString();
}

function bandClasses(band) {
  switch (band) {
    case 'Healthy':
      return 'bg-green-100 text-green-700';
    case 'Needs Attention':
      return 'bg-yellow-100 text-yellow-700';
    case 'At Risk':
      return 'bg-red-100 text-red-700';
    default:
      return 'bg-gray-100 text-gray-600';
  }
}

export default function ClientHealthPanel({ clientId }) {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);
  const [recomputing, setRecomputing] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getLatestHealth(clientId)
      .then((data) => {
        if (!cancelled) setHealth(data);
      })
      .catch((err) => {
        // A 404 just means no score has been computed yet -- not an error.
        if (!cancelled && err.response?.status !== 404) {
          setError('Could not load the health score.');
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [clientId]);

  const handleRecompute = async () => {
    setError('');
    setRecomputing(true);
    try {
      const data = await recomputeHealth(clientId);
      setHealth(data);
    } catch {
      setError('Could not recompute the health score right now.');
    } finally {
      setRecomputing(false);
    }
  };

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h2 className="text-sm font-semibold text-gray-900">Health Score</h2>
          {health && (
            <p className="mt-1 text-xs text-gray-500">Last computed {formatDate(health.computedAt)}</p>
          )}
        </div>
        <button
          type="button"
          onClick={handleRecompute}
          disabled={recomputing}
          className="shrink-0 rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
        >
          {recomputing ? 'Recomputing…' : 'Recompute Health Score'}
        </button>
      </div>

      {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
      {loading && <p className="mt-4 text-sm text-gray-500">Loading health score…</p>}

      {!loading && !health && !error && (
        <p className="mt-4 text-sm text-gray-500">
          No health score has been computed yet. Click "Recompute Health Score" to calculate one.
        </p>
      )}

      {health && (
        <div className="mt-4">
          <span className={`inline-flex rounded-full px-3 py-1 text-sm font-medium ${bandClasses(health.band)}`}>
            Health: {health.score} {health.emoji} · {health.band}
          </span>

          <ul className="mt-4 divide-y divide-gray-200 rounded-lg border border-gray-200">
            {health.breakdown.map((item) => (
              <li key={item.signal} className="flex items-center justify-between px-4 py-2 text-sm">
                <span className="text-gray-600">{item.reason}</span>
                <span className={item.points > 0 ? 'text-green-600' : item.points < 0 ? 'text-red-600' : 'text-gray-400'}>
                  {item.points > 0 ? `+${item.points}` : item.points}
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
