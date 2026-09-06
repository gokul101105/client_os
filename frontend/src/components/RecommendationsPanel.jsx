import { useState } from 'react';
import { getRecommendations } from '../services/aiService';

function priorityClasses(priority) {
  switch (priority) {
    case 'High':
      return 'bg-red-100 text-red-700';
    case 'Medium':
      return 'bg-yellow-100 text-yellow-700';
    default:
      return 'bg-gray-100 text-gray-600';
  }
}

// No chat-history-style accumulation here on purpose: "what should I do"
// is a point-in-time question, so each click replaces the previous
// result rather than appending to a running list.
export default function RecommendationsPanel({ clientId }) {
  const [recommendations, setRecommendations] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleClick = async () => {
    setError('');
    setLoading(true);
    try {
      const data = await getRecommendations(clientId);
      setRecommendations(data.recommendations);
    } catch (err) {
      const message =
        err.code === 'ECONNABORTED'
          ? 'The AI assistant is taking too long to respond. Please try again.'
          : err.response?.data?.message || 'Could not get recommendations right now.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-5">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-sm font-semibold text-gray-900">What should I do?</h2>
        <button
          type="button"
          onClick={handleClick}
          disabled={loading}
          className="shrink-0 rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
        >
          {loading ? 'Thinking…' : 'What should I do?'}
        </button>
      </div>

      {error && <p className="mt-3 text-sm text-red-600">{error}</p>}

      {loading && (
        <p className="mt-4 text-sm text-gray-500">
          Gathering health, summary, and document signals to generate recommendations…
        </p>
      )}

      {!loading && !recommendations && !error && (
        <p className="mt-4 text-sm text-gray-500">
          Combines this client's health score, latest summary, and documents into concrete next
          steps.
        </p>
      )}

      {!loading && recommendations && (
        <ol className="mt-4 space-y-3">
          {recommendations.map((rec, index) => (
            <li key={index} className="rounded-lg border border-gray-200 p-3">
              <div className="flex items-start justify-between gap-3">
                <p className="text-sm font-medium text-gray-900">
                  {index + 1}. {rec.action}
                </p>
                <span
                  className={`shrink-0 rounded-full px-2 py-0.5 text-xs font-medium ${priorityClasses(rec.priority)}`}
                >
                  {rec.priority}
                </span>
              </div>
              <p className="mt-1 text-xs text-gray-500">{rec.reason}</p>
            </li>
          ))}
        </ol>
      )}
    </div>
  );
}
