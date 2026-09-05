import { Link } from 'react-router-dom';

function healthBadgeClasses(score) {
  if (score == null) return 'bg-gray-100 text-gray-600';
  if (score >= 75) return 'bg-green-100 text-green-700';
  if (score >= 50) return 'bg-yellow-100 text-yellow-700';
  return 'bg-red-100 text-red-700';
}

export default function ClientCard({ client }) {
  return (
    <Link
      to={`/clients/${client.id}`}
      className="block rounded-lg border border-gray-200 bg-white p-5 shadow-sm transition hover:shadow-md"
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <h3 className="text-base font-semibold text-gray-900">{client.name}</h3>
          <p className="mt-1 text-sm text-gray-500">{client.industry || 'No industry set'}</p>
        </div>
        <span className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-medium ${healthBadgeClasses(client.healthScore)}`}>
          {client.healthScore != null ? client.healthScore : '—'}
        </span>
      </div>
      <div className="mt-4 flex items-center justify-between text-sm text-gray-500">
        <span>{client.plan}</span>
        <span>
          {client.openIssuesCount ?? 0} open issue{client.openIssuesCount === 1 ? '' : 's'}
        </span>
      </div>
    </Link>
  );
}
