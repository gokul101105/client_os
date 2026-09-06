import { useState } from 'react';
import { getMeetingBrief } from '../services/aiService';

export default function MeetingBriefPanel({ clientId }) {
  const [brief, setBrief] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleClick = async () => {
    setError('');
    setLoading(true);
    try {
      const data = await getMeetingBrief(clientId);
      setBrief(data);
    } catch (err) {
      const message =
        err.code === 'ECONNABORTED'
          ? 'The AI assistant is taking too long to respond. Please try again.'
          : err.response?.data?.message || 'Could not prepare a meeting brief right now.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-5">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-sm font-semibold text-gray-900">Meeting Brief</h2>
        <button
          type="button"
          onClick={handleClick}
          disabled={loading}
          className="shrink-0 rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
        >
          {loading ? 'Preparing…' : 'Prepare Meeting Brief'}
        </button>
      </div>

      {error && <p className="mt-3 text-sm text-red-600">{error}</p>}

      {loading && (
        <p className="mt-4 text-sm text-gray-500">
          Reviewing documents, issues, and meeting history for this client…
        </p>
      )}

      {!loading && !brief && !error && (
        <p className="mt-4 text-sm text-gray-500">
          Pulls together this client's documents, issues, and meeting history into a single brief
          before your next meeting.
        </p>
      )}

      {!loading && brief && (
        <div className="mt-4 space-y-4">
          <BriefSection title="Key Context" text={brief.keyContext} />
          <BriefList title="Agenda Suggestions" items={brief.agendaSuggestions} />
          <BriefList title="Open Issues to Address" items={brief.openIssuesToAddress} />
          <BriefList title="Risks / Watchouts" items={brief.risksOrWatchouts} />

          <p className="border-t border-gray-100 pt-3 text-xs text-gray-400">
            Based on {brief.documentsReviewed} document excerpt{brief.documentsReviewed === 1 ? '' : 's'}.
            {!brief.issuesDataAvailable && ' No ticketing data available yet.'}
            {!brief.meetingsDataAvailable && ' No meeting history tracked yet.'}
          </p>
        </div>
      )}
    </div>
  );
}

function BriefSection({ title, text }) {
  if (!text) return null;
  return (
    <div>
      <h3 className="text-xs font-medium uppercase tracking-wide text-gray-500">{title}</h3>
      <p className="mt-1 text-sm text-gray-700">{text}</p>
    </div>
  );
}

function BriefList({ title, items }) {
  if (!items || items.length === 0) return null;
  return (
    <div>
      <h3 className="text-xs font-medium uppercase tracking-wide text-gray-500">{title}</h3>
      <ul className="mt-1 list-inside list-disc space-y-1 text-sm text-gray-700">
        {items.map((item, index) => (
          <li key={index}>{item}</li>
        ))}
      </ul>
    </div>
  );
}
